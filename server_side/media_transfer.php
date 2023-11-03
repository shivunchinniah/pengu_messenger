<?php

/*

  This REST host manages user authentication and registration
 * 
 * It has the following directories:
 * /get_token
 * /check_username ~check username availability
 * /check_email ~check email availability
 * /register_user
 * /verify_email
 * 

 */

require_once("PenguRESTHost.php");
require_once("UserDatabaseCommunicator.php");
require_once("MediaDatabaseCommunicator.php");
require_once("hidden/ExternalLib/Gump/gump.class.php");
//require_once("PublicKeyDatabaseCommunicator.php");

$host = new PenguRESTHost();


$udc = new UserDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');
$medc = new MediaDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');

//$pudc = new PublicKeyDatabaseCommunicator('localhost', 'root', '', 'pengumessengerserversidedb');






$host->addSub("/upload_media", function() {
    global $medc;
    global $udc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'message_reference' => 'required|integer'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string',
        'message_reference' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);

    function storeFile() {

        // Undefined | Multiple Files | $_FILES Corruption Attack
        // If this request falls under any of them, treat it invalid.
        if (
                !isset($_FILES['upfile']['error']) ||
                is_array($_FILES['upfile']['error'])
        ) {
            echo PenguRESTHost::respondBadRequest();
            die();
        }

        // Check $_FILES['upfile']['error'] value.
        switch ($_FILES['upfile']['error']) {
            case UPLOAD_ERR_OK:
                break;
            case UPLOAD_ERR_NO_FILE:
                echo PenguRESTHost::respondBadRequest();
            case UPLOAD_ERR_INI_SIZE:
            case UPLOAD_ERR_FORM_SIZE:
                $jobj = array(
                    'message' => 'File Too Large'
                );

                $json = json_encode($jobj);
                echo PenguRESTHost::respondForbidden($json);
                die();
            default:
                echo PenguRESTHost::respondServerError();
                die();
        }

        // You should also check filesize here. 
        if ($_FILES['upfile']['size'] > 1400000000) {
            $jobj = array(
                'message' => 'File Too Large > 1.4GB'
            );

            $json = json_encode($jobj);
            echo PenguRESTHost::respondForbidden($json);
            die();
        }

        // DO NOT TRUST $_FILES['upfile']['mime'] VALUE !!
        // Check MIME Type by yourself.
        $finfo = new finfo(FILEINFO_MIME_TYPE);
        //echo $finfo->file($_FILES['upfile']['tmp_name']);

        
        /*
        if (false === $ext = array_search(
                $finfo->file($_FILES['upfile']['tmp_name']), array(
            'jpg' => 'image/jpeg',
            'png' => 'image/png',
            'gif' => 'image/gif',
            'mp4' => 'video/mp4',
            'mov' => 'video/quicktime',
            'pmem' => 'application/pengumessengerencryptedmessage'
                ), true
                )) {
            echo PenguRESTHost::respondBadRequest();
            die();
        }
        */

        $location = sprintf('./hidden/uploads/%s.%s', sha1_file($_FILES['upfile']['tmp_name']), $ext);
        // You should name it uniquely.
        // DO NOT USE $_FILES['upfile']['name'] WITHOUT ANY VALIDATION !!
        // On this example, obtain safe unique name from its binary data.
        if (!move_uploaded_file($_FILES['upfile']['tmp_name'], $location)) {
            echo PenguRESTHost::respondServerError();
            die();
        }
        return $location;
    }

    if ($valid == true) {
        if ($udc->verifyToken($valid['username'], $valid['token'])) {
            if ($medc->verifyUploaderIdentity($valid['username'], $valid['message_reference'])) {

                $messageref = $medc->addFileDirectory(storeFile(), $valid['message_reference']);

                if ($messageref != false) {
                    $jobj = array(
                        'message' => 'Media Uploaded',
                        'media_reference' => $messageref
                    );


                    $json = json_encode($jobj);
                    PenguRESTHost::respondOk($json);
                } else {
                    PenguRESTHost::respondServerError();
                }
            } else {
                PenguRESTHost::respondUnauthorised();
            }
        } else {
            PenguRESTHost::respondUnauthorised();
        }
    } else {

        PenguRESTHost::respondBadRequest();
    }
});


$host->addSub("/download_media", function() {
    global $medc;
    global $udc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'message_reference' => 'required|integer',
        'media_reference' => 'required|integer'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string',
        'message' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);



    if ($valid == true) {
        if ($udc->verifyToken($valid['username'], $valid['token'])) {
            if ($medc->verifyMediaRecipient($valid['message_reference'], $valid['username'], $valid['media_reference'])) {

                if ($medc->mediaIsUploaded($valid['media_reference'])) {

                    $storageDir = $medc->getMediaLocation($valid['media_reference']);
                    if ($storageDir != false) {
                        $filename = $storageDir;

                        if (file_exists($filename)) {

                            //Get file type and set it as Content Type
                            $finfo = finfo_open(FILEINFO_MIME_TYPE);
                            header('Content-Type: ' . finfo_file($finfo, $filename));
                            finfo_close($finfo);

                            //------This forces download in browsers **-------
                            
                            //Use Content-Disposition: attachment to specify the filename
                            header('Content-Disposition: attachment; filename=' . basename($filename));

                            //No cache
                            header('Expires: 0');
                            header('Cache-Control: must-revalidate');
                            header('Pragma: public');
                            
                             
                            
                            //Define file size
                             
                            header('Content-Length: ' . filesize($filename));

                            

                            ///----up to here **--------
                            ob_clean();
                            flush();
                            
                            //readfile($filename);
                            $handle = fopen($filename, 'rb');
                            $buffer = '';
                            while (!feof($handle)) {
                                $buffer = fread($handle, 4096);
                                echo $buffer;
                                ob_flush();
                                flush();
                            }
                            fclose($handle);

                            exit;
                        } else {
                            $jobj = array(
                                'message' => 'File Was Deleted'
                            );

                            $json = json_encode($jobj);
                            echo PenguRESTHost::respondForbidden($json);
                        }
                    } else {
                        PenguRESTHost::respondServerError();
                    }
                } else {
                    $jobj = array(
                        'message' => 'File Was Deleted'
                    );

                    $json = json_encode($jobj);
                    echo PenguRESTHost::respondForbidden($json);
                }
            } else {
                PenguRESTHost::respondUnauthorised();
            }
        } else {
            PenguRESTHost::respondUnauthorised();
        }
    } else {

        PenguRESTHost::respondBadRequest();
    }
});




if ($host->getInstance() == FALSE) {
    //404
    PenguRESTHost::respondNotFound();
}
