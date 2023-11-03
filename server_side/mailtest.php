<?php

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

error_reporting(E_ALL);
ini_set('display_errors', '1');

function sendmail($email, $secretcode) {
        /*
          $to = $email;
          $subject = 'Confirm Email: Pengu Messenger';

          $message = self::getEmailContent($email, $secretcode);

          // To send HTML mail, the Content-type header must be set
          $headers = 'MIME-Version: 1.0' . "\r\n";
          $headers .= 'Content-type: text/html; charset=iso-8859-1' . "\r\n";

          if (mail($to, $subject, $message, $headers)) {
          //echo "email sent";
          return true;
          } else {
          //echo "Fail to send email";
          return false;
          }

         */

        
        

//Load Composer's autoloader
        require 'vendor/autoload.php';

        $mail = new PHPMailer(true);                              // Passing `true` enables exceptions
        try {
            //Server settings
           // $mail->SMTPDebug = 2;                                 // Enable verbose debug output
            $mail->isSMTP();                                      // Set mailer to use SMTP
            $mail->Host = 'www101.jnb2.host-h.net';  // Specify main and backup SMTP servers
            //$mail->Host = 'smpt.diorb.com';  // Specify 
            
            $mail->SMTPAuth = true;                               // Enable SMTP authentication
            $mail->Username = 'pengumessenger-no-reply@diorb.com';                 // SMTP username
            $mail->Password = '&*2nRywYpoU%';                           // SMTP password
            $mail->SMTPSecure = 'tls';                            // Enable TLS encryption, `ssl` also accepted
            $mail->Port = 587;                                    // TCP port to connect to
            //Recipients
            $mail->setFrom('pengumessenger-no-reply@diorb.com', 'Pengu Messenger');
	    $mail->addReplyTo('pengumessenger-no-reply@diorb.com', 'Pengu Messenger');
	    $mail->Sender = 'pengumessenger-no-reply@diorb.com';
            $mail->ContentType = 'text/html';
            $mail->addAddress($email);     // Add a recipient   
            //$mail->addReplyTo('info@example.com', 'Information');
            //$mail->addCC('cc@example.com');
            //$mail->addBCC('bcc@example.com');
            $mail->DKIM_domain = 'diorb.com';
	    $mail->DKIM_private = '/etc/pmta/mailkey.diorb.com.pem';
	    $mail->DKIM_selector = 'mailkey';
            $mail->DKIM_passphrase = '';
	    $mail->DKIM_identity = $mail->From;
            //Attachments
            //$mail->addAttachment('hidden/Pengu Messenger.jpg', 'Pengu Messenger.jpg');         // Add attachments
            //$mail->addAttachment('/tmp/image.jpg', 'new.jpg');    // Optional name
            //Content
            //$mail->addEmbedded('hidden/Pengu Messenger.jpg','Pengu Messenger');
            //$mail->AddEmbeddedImage('hidden/Pengu', 'Pengu Messenger');
            
            $mail->isHTML(true);                                  // Set email format to HTML
            
            $mail->Subject = 'Confirm Email Address: Pengu Messenger';
            $mail->Body = getEmailContent($email, $secretcode);
           
            $mail->AltBody = "Hi, From Pengu Messenger. Here is your verification link: < https://pengumessenger.ddns.net/PenguMessengerServerSide/authenticator.php/verify_email?email=$email&auth_code=$secretcode > Copy this link onto a web-browser URL bar.";

            $mail->send();
           //echo 'Message has been sent';
            return true;
        } catch (Exception $e) {
           // echo 'Message could not be sent. Mailer Error: ', $mail->ErrorInfo;
            return false;
        }
    }


 function getEmailContent($email, $secretcode) {

        return "
        <!DOCTYPE html>
        <html>

<head>
<title>Pengu Messenger Confirmation</title>
</head>

<body style='margin: 0; top: 0'>

    <div class='back' style='background-color: #eee;padding: 10px'>
        <div class='main' style='max-width: 500px;margin-left: auto;margin-right: auto;padding: 10px 20px 10px;background-color: white;border-radius: 5px;margin: 0 auto;text-align: center;box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24)'>
            <h1 style='font-family: sans-serif;color: #666;overflow: hidden; '>Hi, from <span style='color: #007e7e;'>Pengu</span> <span style='color: #b45177'>Messenger</span></h1>
            <div style='text-align: center; width: 85%; margin-left: auto; margin-right: auto'>

                <hr noshade style='color: #d02c2c'>

            </div>
            <br>
            <div class='center2' style='margin-left: auto;margin-right: auto;width: 80%;text-align: center'>
                <p1 style='font-family: monospace;color: black;font-size: 16px;width: 80%; margin-bottom: 10px; text-align: center'>
                    <span style='background-color: aquamarine'>
                    You received this email because your email address was used to sign-up to Pengu Messenger.
                    </span><br><br>
                    <span style='background-color: #ddef00 ;'>If you did not request to sign-up to this service ignore this email.</span> <br><br><span style='background-color: #ff87ba ;'>
                    Please click the button below to confirm that your email address belongs to you.</span>

                </p1><br><br>
                <div style='text-align: center'>

                    <p1 style='font-family: monospace;color: #00af65;font-size: 16px;text-align: center'>Thank you for joining Pengu Messenger!</p1>

                </div>

            </div><br/><br/>
            <a href='https://pengumessenger.ddns.net/PenguMessengerServerSide/authenticator.php/verify_email?email=$email&auth_code=$secretcode'>
                <div class='center' style='text-align: center;border: solid #666;border-radius: 5px;font-family: sans-serif;color: #666;width: 200px;display: inline-block;padding: 10px 0'>
                    <b>Confirm  Registration</b>
                </div>
            </a>
            <br/><br/><br/>


            <img src='https://www.diorb.com/Pengu%20Messenger.jpg' width='300px' alt='Pengu Messenger Logo' style='border-radius: 20px' />

            <br/>

            <br>
            <p1 style='font-family: monospace;color: #8d8d8d;font-size: 12px;width: 80%'> This is a automated email, please do not respond to this email.</p1>
            <br>
            <p style='font-family: monospace;color: #8d8d8d'>&copy;2018 Pengu Messenger<br/>Created by Shivun Chinniah<br/>Powered by diorb.com</p>
        </div>
    </div>

</body>

</html>
";
    }


sendmail("shivunchinniah27@gmail.com", "1234");
