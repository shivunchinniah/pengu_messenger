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
require_once("PublicKeyDatabaseCommunicator.php");

$host = new PenguRESTHost();


$udc = new UserDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');
$pudc = new PublicKeyDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');



$host->addSub("/search_users", function() {
    global $pudc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,1',
        'page' => 'required|integer|min_numeric,1'
    ));
	
    $gump->filter_rules(array(
        'username' => 'sanitize_string',
        'page' => 'trim|sanitize_numbers'
    ));
    
    
    $valid = $gump->run($_POST);

    if ($valid == true) {
	
	$escapedUsername = str_replace("_", "[_]", $valid['username']);
        $results = $pudc->searchByUsername($valid['username'], $valid['page']);

        if ($results == "No results") {
            PenguRESTHost::respondOkNoContent();
        } else if ($results == false) {
            PenguRESTHost::respondServerError();
        } else {


            $jobj = array(
                'message' => 'Search Results',
                'results' => $results
            );

            $json = json_encode($jobj);
            PenguRESTHost::respondOk($json);
        }
    } else {
        PenguRESTHost::respondBadRequest();
    }
});

$host->addSub("/add_public_key", function() {
    global $pudc;
    global $udc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'public_key' => 'required'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string',
        'public_key' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);


    if ($valid == true) {
        if ($udc->verifyToken($valid['username'], $valid['token'])) {

            if ($pudc->addNewPublicKey($valid['public_key'], $valid['username'])) {
                
                PenguRESTHost::respondOkNoContent();
            } else {
                PenguRESTHost::respondServerError();
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
