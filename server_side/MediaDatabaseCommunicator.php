<?php

include_once("DatabaseCommunicator.php");

class MediaDatabaseCommunicator extends DatabaseCommunicator {

    function verifyUploaderIdentity($user,$messageID) {
        if ($this->makeConn()) {



            $sql = "SELECT MessageID FROM ". TBL_MESSAGE. " WHERE MessageID=$messageID AND SenderID=(SELECT UserID FROM " . TBL_USER . " WHERE username='$user')";



            $result = $this->conn->query($sql);

            //var_dump($numofparts,mysqli_fetch_assoc($result), ((int) mysqli_fetch_assoc($result)['NumberOfParts']) >= ((int)$part), (int)$part, 3>=3,(mysqli_fetch_assoc($result)));


            if ($result->num_rows > 0) {
                return true;
            } else {
                echo mysqli_error($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function addFileDirectory($directory, $messageID) {
        if ($this->makeConn()) {



            $sql = "INSERT INTO " . TBL_MESSAGE_MED . " (MessageID, StorageLocation) VALUES ($messageID,'$directory')";

            if ($this->conn->query($sql) === TRUE) {
                $insert_id = $this->conn->insert_id;
                return $insert_id;
            } else {
                echo mysqli_errno($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function verifyMediaRecipient($messageID, $user, $mediaID) {
        if ($this->makeConn()) {



            $sql = "SELECT t1.MessageID FROM (SELECT MessageID FROM " . TBL_MESSAGE . " WHERE MessageID=$messageID AND RecipiantID=(SELECT UserID FROM " . TBL_USER . " WHERE username='$user')) t1 INNER JOIN (SELECT MessageID FROM " . TBL_MESSAGE_MED . " WHERE MediaID=$mediaID) t2 ON t1.MessageID = t2.MessageID";



            $result = $this->conn->query($sql);

            //var_dump($numofparts,mysqli_fetch_assoc($result), ((int) mysqli_fetch_assoc($result)['NumberOfParts']) >= ((int)$part), (int)$part, 3>=3,(mysqli_fetch_assoc($result)));


            if ($result->num_rows > 0) {
                return true;
            } else {
                echo mysqli_error($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function mediaIsUploaded($mediaID) {
        if ($this->makeConn()) {

            $sql = "SELECT MediaID FROM " . TBL_MESSAGE_MED . " WHERE MediaID=$mediaID";

            $result = $this->conn->query($sql);
            if ($result->num_rows > 0) {

                return true;
            } else {
                // mysqli_error($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function getMediaLocation($mediaID) {
        if ($this->makeConn()) {

            $sql = "SELECT StorageLocation FROM " . TBL_MESSAGE_MED . " WHERE MediaID=$mediaID";

            $result = $this->conn->query($sql);
            if ($result->num_rows > 0) {

                return mysqli_fetch_assoc($result)['StorageLocation'];
            } else {
                // mysqli_error($this->conn);
                return false;
            }
        } else {
            return false;
        }
    }

}
