<?php

include_once("DBTables.php");

class DatabaseCommunicator{
    
    //64
    public static function generateToken(){
        return bin2hex(openssl_random_pseudo_bytes(32));
    }
    
    
    protected $conn;
    //protected $table;
    private $server;
    private $username;
    private $password;
    private $db;

    

    function __construct($server, $username, $password, $db){

        $this->server = $server;
        $this->username = $username;
        $this->password = $password;
        $this->db = $db;
        //$this->table = $table;
        


    }
    
    
    
   

    protected function makeConn(){

        
    
            $this->conn = new mysqli($this->server,$this->username,$this->password, $this->db);
        



        if($this->conn->connect_error){
            die("Database Error");
            
            return false;
        }else{
            return true;
        }

    } 


} 

/*

//---Sample Use---

// Create a new Connection
$db = new DatabaseCommunicator("127.0.0.1","pengu","YPgMVRNB6NOQyfI4","pengu");


// Checks if the input username exists
if($db->username_exists("bobisobo")){
    echo "Name exists Exists<br>";
}else{
    echo "Name Available<br>";
}

// Checks if the password is correct
if($db->authenticate("test1","password")){
    echo "Correct Password!<br>";
}

if($db->varify_token("test1","6R_e4L9PxhN7Cxvxf_516zK2dauGH188TtVwuJXbKBE")){
    echo "Correct token!<br>";
}


*/










?>
