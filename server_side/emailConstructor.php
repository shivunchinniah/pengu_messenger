<?php


function getEmailContent($email, $secretcode){
    
    return "<html>

<head>
    </head>

<body>

    <div class='back' style='background-color: #eee;padding: 10px'>
        <div class='main' style='max-width: 500px;margin-left: auto;margin-right: auto;padding: 10px 20px 10px;background-color: white;border-radius: 5px;margin: 0 auto;text-align: center;box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24)'>
            <h1 style='font-family: sans-serif;color: #666;overflow: hidden'>Hi, from <span style='color: #007e7e;'>Pengu</span> <span style='color: #b45177'>Messenger</span></h1>
             <div class='center2' style='margin-left: auto;margin-right: auto;width: 80%;text-align: left'><p1 style='font-family: monospace;color: #8d8d8d;font-size: 16px;width: 80%'>You received this email because your email address was used to sign-up to Pengu Messenger. If you did not request to sign-up to this service ignore this email.</p1></div><br/><br/>
            <a href='http://localhost/PenguMessengerServerSide/authenticate.php/verify_email?email=$email&auth_code=$secretcode'>
                <div class='center' style='text-align: center;border: solid #666;border-radius: 5px;font-family: sans-serif;color: #666;width: 200px;display: inline-block;padding: 10px 0'>
                    <b>Click to confirm  Email</b>
                </div></a>
            <br/><br/><br/>
            
            
            <img src='http://diorb.com/img/Pengu Messenger.jpg' width='300px' style='max-width: 85%;border-radius: 20px'/>
            <br/>
            <p style='font-family: monospace;color: #8d8d8d'>©2018 Pengu Messenger<br/>Created by Shivun Chinniah</p>
        </div>
    </div>

</body>

</html>";
    
    
    
}
