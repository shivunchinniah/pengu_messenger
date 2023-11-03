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
require_once("hidden/ExternalLib/Gump/gump.class.php");
require_once("DBTables.php");
require_once("PenguEncryption.php");


$host = new PenguRESTHost();


$dc = new UserDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');


$host->addsub("/get_token", function() {

    global $dc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'password' => 'required|max_len,64|min_len,6'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);

    if ($valid == true) {
        if ($dc->authenticate($valid['username'], $valid['password'])) {
            $gentoken = DatabaseCommunicator::generateToken();

            if ($dc->updateToken($valid['username'], $gentoken)) {


                if ($dc->emailVerified($dc->getEmailByUsername($valid['username']))) {

                    $jobj = array(
                        'message' => 'Access Granted',
                        'token' => $gentoken,
                    );

                    $json = json_encode($jobj);

                    PenguRESTHost::respondOk($json);
                } else {

                    $jobj = array(
                        'message' => 'Email Not Verified'
                    );

                    $json = json_encode($jobj);

                    PenguRESTHost::respondForbidden($json);
                }
            } else {
                PenguRESTHost::respondServerError();
            }
        } else {
            $jobj = array(
                'message' => 'Invalid Credentials'
            );

            $json = json_encode($jobj);

            PenguRESTHost::respondForbidden($json);
        }
    } else {
        PenguRESTHost::respondBadRequest();
    }
});


$host->addsub("/register_user", function() {
    global $dc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'email' => 'required|valid_email',
        'password' => 'required|max_len,64|min_len,6'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'password' => 'trim',
        'email' => 'trim|sanitize_email'
    ));

    $valid = $gump->run($_POST);

    if ($valid == true) {


        if ($dc->usernameExists($valid['username'])) {
            // Username exists
            $jobj = array(
                'message' => 'Username exists'
            );

            $json = json_encode($jobj);
            PenguRESTHost::respondForbidden($json);
        } else if ($dc->emailExists($valid['email'])) {
            // Email exists
            $jobj = array(
                'message' => 'Email exists'
            );

            $json = json_encode($jobj);
            PenguRESTHost::respondForbidden($json);
        } else {
            $serverkey = PenguEncrypt::generateSecretKey();
            $encryptedserverkey = PenguEncrypt::encrypt($valid['password'], $serverkey);
            $actualkey = PenguEncrypt::create_combinedkey($valid['password'] . $serverkey);
            $secretcode = bin2hex(openssl_random_pseudo_bytes(12));
            $secretkeyUserCreated = $dc->addUser($valid['username'], $valid['email'], $valid['password'],$secretcode) && $dc->addUserServerKey($valid['username'], $encryptedserverkey, $actualkey);
            
            if ( $secretkeyUserCreated != false) {


                $jobj = array(
                    'message' => 'User successfully added, email is not verified'
                );

                $json = json_encode($jobj);
                PenguRESTHost::respondAccepted($json);
                UserDatabaseCommunicator::sendmail($valid['email'], $secretcode);
                
            } else {
               	 echo "failed to add user *remove*";
		 PenguRESTHost::respondServerError();
            }
        }
    } else {
        PenguRESTHost::respondBadRequest();
    }
});


$host->addsub("/verify_email", function() {
    //sleep(1);
    global $dc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_GET);

    $gump->validation_rules(array(
        'email' => 'required|valid_email',
        'auth_code' => 'required|min_len,24|max_len,24'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_email',
        'auth_code' => 'trim'
    ));

    $valid = $gump->run($_GET);

    if ($valid == true) {
        if ($dc->checkEmailSecretCode($valid['email'], $valid['auth_code'])) {
            if ($dc->removeVerifiedEmail($valid['email'])) {

                //echo "Email has been verified.";
                PenguRESTHost::respondOkNoContent();
            } else {
                PenguRESTHost::respondServerError();
            }
        } else {
            //404 page
            PenguRESTHost::respondNotFound();
            //
        }
    } else {
        //404 page
        PenguRESTHost::respondNotFound();
        //report_error("Invalid Request");
    }
});


if ($host->getInstance() == FALSE) {
    PenguRESTHost::respondNotFound();
}
?>
