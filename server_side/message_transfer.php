<?php
ini_set('display_errors', 1);
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
require_once("MessageDatabaseCommunicator.php");
require_once("hidden/ExternalLib/Gump/gump.class.php");
//require_once("PublicKeyDatabaseCommunicator.php");

$host = new PenguRESTHost();


$udc = new UserDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');
$mdc = new MessageDatabaseCommunicator('localhost', 'pengu', 'VPsdJeAHkgHU', 'pengumessenger');

//$pudc = new PublicKeyDatabaseCommunicator('localhost', 'root', '', 'pengumessengerserversidedb');



$host->addSub("/send_message_parent", function() {
    global $mdc;
    global $udc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'message' => 'required|max_len,256', // Max message part content length: 256 characters
        'parts' => 'required|integer|max_numeric,88|min_numeric,1', //Max message Length is 256*88 = 22 500 characters long
        'recipient' => 'required|alpha_dash_num|max_len,30|min_len,6'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'recipient' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string',
        'message' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);


    if ($valid == true) {
        if ($udc->verifyToken($valid['username'], $valid['token'])) {
            if ($udc->usernameExists($valid['recipient'])) {
                $result = $mdc->addMessageParent($valid['username'], $valid['message'], $valid['parts'], $valid['recipient']);
                if ($result != FALSE) {
                    $jobj = array(
                        'message' => 'Message Sent',
                        'part' => '1',
                        'total_parts' => $valid['parts'],
                        'message_reference' => $result
                    );

                    $json = json_encode($jobj);
                    PenguRESTHost::respondOk($json);
                } else {
                    PenguRESTHost::respondServerError();
                }
            } else {

                $jobj = array(
                    'message' => 'Recipient Username Does Not Exist'
                );

                $json = json_encode($jobj);
                PenguRESTHost::respondForbidden($json);
            }
        } else {

            PenguRESTHost::respondUnauthorised();
        }
    } else {

        PenguRESTHost::respondBadRequest();
    }
});


$host->addSub("/send_message_child", function() {
    global $mdc;
    global $udc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'message' => 'required|max_len,256', // Max message part content length: 256 characters
        'part_number' => 'required|integer|max_numeric,4000000|min_numeric,2', //Max number of parts is 4 Million ~ 1GB in content
        'message_reference' => 'required|integer'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string',
        'message' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);


    if ($valid == true) {
        if ($udc->verifyToken($valid['username'], $valid['token'])) {
            if ($mdc->verifyChildMessage($valid['message_reference'], $valid['username'], $valid['part_number'])) {
                $result = $mdc->addMessageChild($valid['message'], $valid['part_number'], (int) $valid['message_reference']);
                if ($result != FALSE) {
                    $jobj = array(
                        'message' => 'Message Sent',
                        'part' => $valid['part_number'],
                        'total_parts' => $result,
                        'message_reference' => $valid['message_reference']
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


$host->addSub("/message_summary", function() {
    global $mdc;
    global $udc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);


    if ($valid == true) {
        if ($udc->verifyToken($valid['username'], $valid['token'])) {
            $results = $mdc->userMessageSummary($valid['username']);

            if ($results == "No Results") {
                PenguRESTHost::respondOkNoContent();
            } else if ($results == false) {
                PenguRESTHost::respondServerError();
            } else {


                $jobj = array(
                    'message' => 'Inbound Message Summary',
                    'summary' => $results
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

$host->addSub("/get_message_parts", function() {
    global $mdc;
    global $udc;

    $gump = new GUMP();

    $_POST = $gump->sanitize($_POST);

    $gump->validation_rules(array(
        'username' => 'required|alpha_dash_num|max_len,30|min_len,6',
        'token' => 'required|alpha_numeric|min_len,64|max_len,256',
        'message_reference' => 'required|integer|min_len,1'
    ));

    $gump->filter_rules(array(
        'username' => 'trim|sanitize_string',
        'token' => 'trim|sanitize_string'
    ));

    $valid = $gump->run($_POST);


    if ($valid == true) {
        if ($udc->verifyToken($valid['username'], $valid['token'])) {
            $sender = $mdc->verifyMessageRecipient($valid['message_reference'], $valid['username']);
            if ($sender != FALSE) {
                $results = $mdc->getMessagePartsByID($valid['message_reference']);
                if ($results != FALSE) {
                    $jobj = array(
                        'message' => 'Message Parts',
                        'message_reference' => $valid['message_reference'],
                        'message_size' => count($results),
                        'message_parts' => $results,
                        'sender' => $sender
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


if ($host->getInstance() == FALSE) {
    //404
    PenguRESTHost::respondNotFound();
}
