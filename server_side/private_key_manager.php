<?php

require_once("PenguRESTHost.php");
require_once("UserDatabaseCommunicator.php");
require_once("hidden/ExternalLib/Gump/gump.class.php");
require_once("PublicKeyDatabaseCommunicator.php");
require_once("PrivateKeyDatabaseCommunicator.php");

$host = new PenguRESTHost();



$server_address = 'localhost';
$server_user = 'root';
$server_password = '';
$server_db = 'pengumessengerserversidedb';

$udc = new UserDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');
$pudc = new PublicKeyDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');
$prdc = new PrivateKeyDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');

$host->addSub("/add_private_key", function() {
    global $udc;
    global $pudc;
    global $prdc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'private_key' => 'required|regex,([a-zA-Z0-9+/=#]$)',
        'password' => 'required|max_len,64|min_len,6'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string',
        'public_key' => 'trim|sanitize_string',
        'private_key' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);

    function addPrivateKey($prdc, $valid) {
        include_once("PenguEncryption.php");


        $encserverkey = $prdc->getEncryptedServerKey($valid['username']);
        //echo var_dump($encserverkey);
        if ($encserverkey) {
            $serverkey = PenguEncrypt::decrypt($valid['password'], $encserverkey);
            $actualkey = PenguEncrypt::create_combinedkey($valid['password'] . $serverkey);

            $encryptedprivatekey = PenguEncrypt::multiFactorEncrypt($actualkey, $serverkey, $valid['private_key']);

            if ($serverkey != false) {

                if ($prdc->addNewPrivateKey($encryptedprivatekey, $valid['username'])) {


                    PenguRESTHost::respondOkNoContent();
                } else {
                    // can't add private key
                    PenguRESTHost::respondServerError();
                }
            } else {
                // can't decrypt key
                $jobj = array(
                    'message' => 'The Server Key Has Been Corrupted, Please Contact Administrator'
                );

                $json = json_encode($jobj);
                PenguRESTHost::respondForbidden($json);
            }
        } else {
            //can't fetch server key
            PenguRESTHost::respondServerError();
        }
    }

    if ($valid == true) {

        if ($udc->authenticate($valid['username'], $valid['password']) && $udc->verifyToken($valid['username'], $valid['token'])) {
            //echo var_dump($valid['public_key']);
            //$pkExist = $pudc->publicKeyExists($valid['public_key']);
            //echo var_dump($pkExist);

            addPrivateKey($prdc, $valid, $pudc, false);
        } else {
    
            PenguRESTHost::respondUnauthorised();
        }
    } else {
        //echo json_encode($_POST);
        PenguRESTHost::respondBadRequest();
    }
});

$host->addSub("/get_private_keys", function() {
    global $udc;
    global $prdc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'password' => 'required|max_len,64|min_len,6'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);

    if ($valid == true) {

        if ($udc->authenticate($valid['username'], $valid['password']) && $udc->verifyToken($valid['username'], $valid['token'])) {

            $results = $prdc->getPrivateKeys($valid['username']);

            if ($results == "No results") {  
                PenguRESTHost::respondOkNoContent();
            } else if ($results == false) {
                PenguRESTHost::respondServerError();
            } else {
                include_once("PenguEncryption.php");
                $encserverkey = $prdc->getEncryptedServerKey($valid['username']);
                $serverKey = PenguEncrypt::decrypt($valid['password'], $encserverkey);

                $actualkey = PenguEncrypt::create_combinedkey($valid['password'] . $serverKey);

                $temp = array();

                for ($i = 0; $i < count($results); $i++) {
                    $decrypted = PenguEncrypt::multiFactorDecrypt($actualkey, $serverKey, $results[$i]['private_key']);
                    if ($decrypted != false || 1 == 1) {
                        $results[$i]['private_key'] = $decrypted;
                        array_push($temp, $results[$i]);
                    }
                }

                $jobj = array(
                    'message' => 'Private Keys',
                    'results' => $temp
                );

                $json = json_encode($jobj);
                PenguRESTHost::respondOk($json);
            }
        } else {
            
            PenguRESTHost::respondUnauthorised();
        }
    } else {
        
        PenguRESTHost::respondBadRequest();
       
    }
});

$host->addSub("/request_recovery_key", function() {
    global $udc;
    global $pudc;
    global $prdc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'password' => 'required|max_len,64|min_len,6'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);

    if ($valid == true) {

        if ($udc->authenticate($valid['username'], $valid['password']) && $udc->verifyToken($valid['username'], $valid['token'])) {

            include_once("PenguEncryption.php");
            $encserverkey = $prdc->getEncryptedServerKey($valid['username']);
            $serverKey = PenguEncrypt::decrypt($valid['password'], $encserverkey);

            $actualkey = PenguEncrypt::create_combinedkey($valid['password'] . $serverKey);

            $recoverykey = $serverKey . $actualkey;

            $temp = array();

            if ($encserverkey && $serverKey != false) {

                $jobj = array(
                    'message' => 'Store In Secure Place',
                    'recovery_key' => $recoverykey
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
        //echo json_encode($_POST);
        PenguRESTHost::respondBadRequest();
    }
});




if ($host->getInstance() == FALSE) {
    //404
    PenguRESTHost::respondNotFound();
}