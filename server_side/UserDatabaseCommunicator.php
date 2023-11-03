<?php
// Import PHPMailer classes into the global namespace
// These must be at the top of your script, not inside a function
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

include("DatabaseCommunicator.php");

class UserDatabaseCommunicator extends DatabaseCommunicator {

    function usernameExists($username) {


        if ($this->makeConn()) {

            $sql = "SELECT username FROM " . TBL_USER . " WHERE username='$username' ;";

            $result = $this->conn->query($sql);

	    echo mysqli_error($this->conn);
            if ($result->num_rows > 0) {
                return true;
            } else {
                return false;
            }


            $this->conn->close();
        } else {
            return true;
        }
    }

    function emailExists($email) {

        if ($this->makeConn()) {

            $sql = "SELECT email FROM " . TBL_USER . " WHERE email='$email' ;";

            $result = $this->conn->query($sql);


            if ($result->num_rows > 0) {
                return true;
            } else {
                return false;
            }


            $this->conn->close();
        } else {
            return true;
        }
    }

    function authenticate($username, $inputpassword) {

        if ($this->makeConn()) {

            $sql = "SELECT passwordhash FROM " . TBL_USER . " WHERE username='$username'";



            $result = $this->conn->query($sql);

            if ($result->num_rows > 0) {

                $password = $result->fetch_assoc()['passwordhash'];

                //echo $password;
                //echo $inputpassword;


                if (password_verify($inputpassword, $password)) {


                    return true;
                } else {

                    return false;
                }
            } else {
                return false;
            }



            $this->conn->close();
        } else {
            return false;
        }
    }

    function updateToken($username, $token) {
        if ($this->makeConn()) {

            $hashtoken = password_hash($token, PASSWORD_DEFAULT);
            $token_date = date("Y-m-d H:i:s");

            $sql = "UPDATE " . TBL_USER . " SET authenticationtokenhash='$hashtoken', tokencreationdate='$token_date' WHERE username='$username'";

            if ($this->conn->query($sql) === TRUE) {
                return true;
            } else {
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function verifyToken($username, $inputtoken) {
        if ($this->makeConn()) {

            $sql = "SELECT authenticationtokenhash FROM " . TBL_USER . " WHERE TIMESTAMPDIFF(hour, TokenCreationDate, now())<=12 AND username='$username'";

            $result = $this->conn->query($sql);

            echo mysqli_error($this->conn);
            //see if record exists
            if ($result->num_rows > 0) {

                $token = $result->fetch_assoc()['authenticationtokenhash'];

                /* $token_date = $result->fetch_assoc()['TokenCreationDate'];

                  echo var_dump($token_date) ."<br>";

                  //tokens are valid for 12 hours
                  $expired = strtotime($token_date . ' + 0 hours');
                  $now = strtotime(date("Y-m-d H:i:s"));

                  echo "$now and $expired and $token_date and $token";
                  //echo $password;
                  //echo $inputpassword;
                 */

                if (password_verify($inputtoken, $token)) {


                    return true;
                } else {

                    return false;
                }
            } else {

                return false;
            }



            $this->conn->close();
        } else {
            return false;
        }
    }

    function addUser($username, $email, $password, $secretcode) {
        if ($this->makeConn()) {

            $hashpass = password_hash($password, PASSWORD_DEFAULT);
            //$secretcode = bin2hex(openssl_random_pseudo_bytes(12));

            $sql = "INSERT INTO " . TBL_USER . " (username, email, passwordhash) VALUES (LCASE('$username'),LCASE('$email'),'$hashpass'); ";

            $sql2 = "INSERT INTO " . TBL_PEV . " (SecretCode, UserID, RequestDate) VALUES('" . $secretcode . "', (SELECT UserID FROM " . TBL_USER . " WHERE username='$username' and email='$email'), UTC_TIMESTAMP() );";

            if ($this->conn->query($sql) === TRUE) {
                  echo mysqli_error($this->conn);
		  if ($this->conn->query($sql2) === TRUE) {
                    //could send email here

                    return true;
                } else {
                    echo mysqli_error($this->conn);
                    return false;
                }
            } else {
                echo mysqli_error($this->conn);
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    function addUserServerKey($username, $encryptedserverkey, $masterkey) {
        if ($this->makeConn()) {

            $masterkeyhash = password_hash($masterkey, PASSWORD_DEFAULT);


            $sql = "UPDATE " . TBL_USER . " SET ServerKey='$encryptedserverkey', MasterKeyHash='$masterkeyhash' WHERE username='$username';";



            if ($this->conn->query($sql) === TRUE) {
                return true;
            } else {
                //echo mysqli_error($this->conn);
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    function getEmailByUsername($username) {
        if ($this->makeConn()) {



            $sql = "SELECT email FROM " . TBL_USER . " WHERE username='$username'";

            $result = $this->conn->query($sql);

            if ($result) {
                return mysqli_fetch_assoc($result)['email'];
            } else {
                echo mysqli_error($this->conn);
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    function emailVerified($email) {
        if ($this->makeConn()) {



            $sql = "SELECT VerificationID FROM " . TBL_PEV . " INNER JOIN " . TBL_USER . " ON " . TBL_USER . ".UserID=" . TBL_PEV . ".UserID  WHERE email='$email'";

            $result = $this->conn->query($sql);

            if ($result) {
                
            } else {

                //echo mysqli_error($this->conn);
                return false;
            }

            if ($result->num_rows > 0) {
                return false;
            } else {
                return true;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    function checkEmailSecretCode($email, $secretcode) {
        if ($this->makeConn()) {



            $sql = "SELECT Email , SecretCode FROM " . TBL_PEV . " INNER JOIN " . TBL_USER . " ON " . TBL_USER . ".UserID=" . TBL_PEV . ".UserID WHERE email='$email' and secretcode='$secretcode'";

            $result = $this->conn->query($sql);

            if ($result) {
                
            } else {

                //echo mysqli_error($this->conn);
                return false;
            }

            if ($result->num_rows > 0) {
                //valid 
                return true;
            } else {
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    function removeVerifiedEmail($email) {
        if ($this->makeConn()) {



            $sql = "DELETE FROM " . TBL_PEV . " WHERE " . TBL_PEV . ".UserID=(SELECT UserID FROM " . TBL_USER . " WHERE email='$email')";

            if ($this->conn->query($sql) === TRUE) {
                return true;
            } else {
                //echo mysqli_errno($this->conn);
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    //cronjob once a day
    function removeExpiredEmails() {
        if ($this->makeConn()) {



            $sql = "DELETE FROM " . TBL_USER . "  WHERE UserID in((Select UserID FROM " . TBL_PEV . " WHERE DATEDIFF(now(), RequestDate)>= 2))";

            if ($this->conn->query($sql) === TRUE) {
                return true;
            } else {
                //echo mysqli_errno($this->conn);
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    //cronjob once a day
    function addtestUser($name, $lastname, $email, $age) {
        if ($this->makeConn()) {



            $sql = "DELETE FROM" . TBL_PEV . " ";

            if ($this->conn->query($sql) === TRUE) {
                return true;
            } else {
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }

    
    public static function sendmail($email, $secretcode) {
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
            $mail->Body = self::getEmailContent($email, $secretcode);
           
            $mail->AltBody = "Hi, From Pengu Messenger. Here is your verification link: < https://pengumessenger.ddns.net/PenguMessengerServerSide/authenticator.php/verify_email?email=$email&auth_code=$secretcode > Copy this link onto a web-browser URL bar.";

            $mail->send();
           //echo 'Message has been sent';
            return true;
        } catch (Exception $e) {
           // echo 'Message could not be sent. Mailer Error: ', $mail->ErrorInfo;
            return false;
        }
    }

    private static function getEmailContent($email, $secretcode) {

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

}





?>
