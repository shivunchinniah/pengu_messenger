<?php

include_once("DatabaseCommunicator.php");

class PublicKeyDatabaseCommunicator extends DatabaseCommunicator {

    //returns false if query failed, else returns array of results
    function searchByUsername($username, $pageNum) {
        if ($this->makeConn()) {

            $start = (($pageNum - 1) * 25);

            //"SELECT username FROM " . TBL_USER . " WHERE username LIKE '%$username%' LIMIT $start,25 ;";
            
            $sql = "SELECT t0.username, t2.publickeystring as public_key , t2.PublicKeyRegistrationDate as registration_date, t2.PublicKeyExpiryDate as expiry_date FROM
(SELECT username, ".TBL_USER.".userID FROM ".TBL_USER." WHERE username like '%$username%') t0
LEFT JOIN 
(SELECT ".TBL_PUK.".UserID, publickeystring, PublicKeyRegistrationDate, PublicKeyExpiryDate FROM ".TBL_PUK." INNER JOIN 
(SELECT userID , MAX(PublicKeyRegistrationDate) as max FROM ".TBL_PUK." GROUP BY userID ) t1
 
ON t1.userID = ".TBL_PUK.".userID and PublicKeyRegistrationDate = t1.max) t2 ON t2.UserID = t0.userID WHERE t2.publickeystring IS NOT NULL LIMIT $start,25";

            $result = $this->conn->query($sql);
            
            if (mysqli_num_rows($result) > 0) {

                //echo (json_encode(mysqli_fetch_all($result, MYSQLI_ASSOC)));

                return mysqli_fetch_all($result, MYSQLI_ASSOC);
            } else {
                return "No results";
            }




            $this->conn->close();
        } else {
            return false;
        }
    }

    function addNewPublicKey($publickey, $username) {
        if ($this->makeConn()) {



            $sql = "INSERT INTO ".TBL_PUK." (PublicKeyExpiryDate,PublicKeyRegistrationDate,PublicKeyString,UserID) VALUES (ADDDATE(UTC_TIMESTAMP(),30),UTC_TIMESTAMP(),'$publickey',(SELECT UserID FROM ".TBL_USER." WHERE username = '$username'))";

            if ($this->conn->query($sql) === TRUE) {
                
                //echo mysqli_errno($this->conn);
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
    
    function removePublicKey($publickey){
        if ($this->makeConn()) {

            $sql = "DELETE FROM " . TBL_PUK . " WHERE PublicKeyString='$publickey' ;";

            


            if ($this->conn->query($sql)) {
                return true;
            } else {
                return false;
            }


            $this->conn->close();
        } else {
            return true;
        }
    }
    
    function publicKeyExists($publicKey) {


        if ($this->makeConn()) {

            $sql = "SELECT PublicKeyString FROM " . TBL_PUK . " WHERE PublicKeyString='$publicKey' ;";

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
    
    
    

}
