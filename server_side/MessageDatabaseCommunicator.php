<?php

include_once("DatabaseCommunicator.php");

class MessageDatabaseCommunicator extends DatabaseCommunicator {

    function addMessageParent($username, $message, $parts, $recipiant) {
        if ($this->makeConn()) {



            $sql = "INSERT INTO " . TBL_MESSAGE . " (NumberOfParts,RecipiantID,SenderID,ServiceDate) VALUES ($parts,(SELECT UserID FROM " . TBL_USER . " WHERE username = '$recipiant'),(SELECT UserID FROM " . TBL_USER . " WHERE username = '$username'),UTC_TIMESTAMP())";

            if ($this->conn->query($sql) === TRUE) {
                $insert_id = $this->conn->insert_id;
                $sql = "INSERT INTO " . TBL_MESSAGE_PRT . " (PartNumber, MessageID, PartContent) VALUES (1, $insert_id, '$message')";
                if ($this->conn->query($sql) === TRUE) {
                    return $insert_id;
                } else {
                    return false;
                }
            } else {
                echo mysqli_errno($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function addMessageChild($message, $part, $messageID) {
        if ($this->makeConn()) {



            $sql = "INSERT INTO " . TBL_MESSAGE_PRT . " (PartNumber, MessageID, PartContent) VALUES ($part,$messageID,'$message')";

            if ($this->conn->query($sql) === TRUE) {
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

    function verifyChildMessage($messageID, $user, $part) {
        if ($this->makeConn()) {



            $sql = "SELECT NumberOfParts FROM " . TBL_MESSAGE . " WHERE MessageID=$messageID AND SenderID=(SELECT UserID FROM " . TBL_USER . " WHERE username='$user')";



            $result = $this->conn->query($sql);

            //var_dump($numofparts,mysqli_fetch_assoc($result), ((int) mysqli_fetch_assoc($result)['NumberOfParts']) >= ((int)$part), (int)$part, 3>=3,(mysqli_fetch_assoc($result)));


            if ($result->num_rows > 0) {
                $numofparts = (int) mysqli_fetch_assoc($result)['NumberOfParts'];
                if ($numofparts >= ((int) $part)) {

                    return $numofparts;
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

    function verifyMessageRecipient($messageID, $user) {
        if ($this->makeConn()) {



            $sql = "SELECT Username FROM " . TBL_USER . " INNER JOIN  (SELECT SenderID FROM " . TBL_MESSAGE . " WHERE MessageID='$messageID' AND RecipiantID=CAST((SELECT UserID FROM " . TBL_USER . " WHERE Username='$user') AS SIGNED) ) t ON SenderID=UserID ";



            $result = $this->conn->query($sql);

            //var_dump($numofparts,mysqli_fetch_assoc($result), ((int) mysqli_fetch_assoc($result)['NumberOfParts']) >= ((int)$part), (int)$part, 3>=3,(mysqli_fetch_assoc($result)));


            if ($result->num_rows > 0) {
                $sender = mysqli_fetch_assoc($result)['Username'];
                return $sender;
            } else {
                echo mysqli_error($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function getMessagePartsByID($messageID) {
        if ($this->makeConn()) {



            $sql = "SELECT PartNumber as part_number, PartContent as content FROM " . TBL_MESSAGE_PRT . " WHERE MessageID=$messageID ORDER BY PartNumber";



            $result = $this->conn->query($sql);

            //var_dump($numofparts,mysqli_fetch_assoc($result), ((int) mysqli_fetch_assoc($result)['NumberOfParts']) >= ((int)$part), (int)$part, 3>=3,(mysqli_fetch_assoc($result)));


            if ($result->num_rows > 0) {

                $sql = "UPDATE " . TBL_MESSAGE . " SET RECIEVED=1, ServiceDate=UTC_TIMESTAMP() WHERE MessageID='$messageID' ";
                $this->conn->query($sql);
                return mysqli_fetch_all($result, MYSQLI_ASSOC);
            } else {
                //echo mysqli_error($this->conn);
                return false;
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

    function messageIsComplete($messageID) {
        if ($this->makeConn()) {



            $sql = "SELECT " . TBL_MESSAGE . ".MessageID as message_reference, NumberOfParts as message_size FROM " . TBL_MESSAGE . " INNER JOIN " . TBL_MESSAGE_PRT . " ON " . TBL_MESSAGE . ".MessageID=" . TBL_MESSAGE_PRT . ".MessageID WHERE RecipiantID=(SELECT UserID FROM " . TBL_USER . " WHERE Username='$username') AND MessageID='$messageID' GROUP BY " . TBL_MESSAGE_PRT . ".MessageID HAVING COUNT(" . TBL_MESSAGE_PRT . ".PartID)=NumberOfParts";



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

    function userMessageSummary($username) {
        if ($this->makeConn()) {



            $sql = "SELECT " . TBL_MESSAGE . ".MessageID as message_reference, NumberOfParts as message_size, Username as username FROM " . TBL_MESSAGE . " INNER JOIN " . TBL_MESSAGE_PRT . " ON " . TBL_MESSAGE . ".MessageID=" . TBL_MESSAGE_PRT . ".MessageID INNER JOIN ". TBL_USER ." ON ".TBL_USER.".UserID=".TBL_MESSAGE.".SenderID WHERE RecipiantID=(SELECT UserID FROM " . TBL_USER . " WHERE Username='$username') GROUP BY " . TBL_MESSAGE_PRT . ".MessageID HAVING COUNT(" . TBL_MESSAGE_PRT . ".PartID)=NumberOfParts";



            $result = $this->conn->query($sql);


            //error_log(mysqli_error($this->conn));

            if ($result->num_rows > 0) {

                return mysqli_fetch_all($result, MYSQLI_ASSOC);
            } else {
                echo mysqli_error($this->conn);
                return "No Results";
            }

            $this->conn->close();
        } else {
            return false;
        }
    }

}
