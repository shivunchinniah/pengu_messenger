<?php

include_once("DatabaseCommunicator.php");

class PrivateKeyDatabaseCommunicator extends DatabaseCommunicator {

    function addNewPrivateKey($encryptedprivatekey, $username) {
        if ($this->makeConn()) {

            

            $sql = "INSERT INTO " . TBL_PRK . " (PrivateKeyString,UserID,PrivateKeyRegistrationDate,PrivateKeyExpiryDate) VALUES ('$encryptedprivatekey',(SELECT UserID FROM " . TBL_USER . " WHERE username = '$username'),UTC_TIMESTAMP(),ADDDATE(UTC_TIMESTAMP(),30))";

            if ($this->conn->query($sql) === TRUE) {

                //echo mysqli_errno($this->conn);
                return true;
            } else {
                echo mysqli_errno($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }
    
    
    function getEncryptedServerKey($username){
         if ($this->makeConn()) {


            
            $sql = "SELECT ServerKey FROM " . TBL_USER . " WHERE username='$username'";

            $result = $this->conn->query($sql);

            if ($result) {
                //echo var_dump($result);
                return mysqli_fetch_assoc($result)['ServerKey'];
            } else {
                //echo mysqli_error($this->conn);
                return false;
            }


            $this->conn->close();
        } else {
            return false;
        }
    }
    
    function getPrivateKeys($username) {
        if ($this->makeConn()) {

        
            
            $sql = "SELECT PrivateKeyId as id, PrivateKeyString as private_key, PrivateKeyRegistrationDate as registration_date, PrivateKeyExpiryDate as expiry_date FROM ".TBL_PRK." WHERE userID = (SELECT userID FROM ".TBL_USER." WHERE username='$username')";

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

}
