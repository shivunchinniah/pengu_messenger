<?php

class PenguRESTHost {

    const OK_GENERAL = 200;
    const ACCEPTED = 202;
    const OK_REMOVAL = 204;
    const BAD_REQUEST = 400;
    const UNAUTHORISED = 401;
    const FORBIDDEN = 403;
    const NOT_FOUND = 404;
    const SERVER_ERROR = 500;
    const SERVICE_UNAVAILABLE = 503;

    public static function getStatusByCode(int $code) {
        switch ($code) {
            case 200:
                return "OK";
            case 202:
                return "ACCEPTED";
            case 204:
                return "OK NO CONTENT";
            case 400:
                return "BAD REQUEST";
            case 401:
                return "UNAUTHORISED";
            case 403:
                return "FORBIDDEN";
            case 404:
                return "NOT FOUND";
            case 500:
                return "SERVER ERROR";
            case 503:
                return "SERVICE UNAVAILABLE";
        }
    }

    public static function getCodeByStatus($status) {
        switch ($status) {
            case "OK":
                return 200;
            case "ACCEPTED":
                return 202;
            case "OK NO CONTENT":
                return 204;
            case "BAD REQUEST":
                return 400;
            case "UNAUTHORISED":
                return 401;
            case "FORBIDDEN":
                return 403;
            case "NOT FOUND":
                return 404;
            case "SERVER ERROR":
                return 500;
            case "SERVICE UNAVAILABLE":
                return 503;
        }
    }

    private $arrSubDirectory = array();

    public static function respondJSON(int $code, $content, bool $dataExists) {
        $status = self::getStatusByCode($code);
        if ($dataExists) {
            $data = $content;
            
            echo "{\"code\": $code, \"status\": \"$status\", \"data\": $data}";
        }else{
            echo "{\"code\": $code, \"status\": \"$status\"}";
        }
    }
    
    public static function respondNotFound(){
        self::respondJSON(404, null, false);
    }
    
    //request was successful
    public static function respondOk($data){
        self::respondJSON(200, $data, true);
    }
    
    //request was successful but is being processed
    public static function respondAccepted($data){
        self::respondJSON(202, $data, true);
    }
    
    //request successful no further data needed
    public static function respondOkNoContent(){
        self::respondJSON(204, null, false);
    }
    
    public static function respondServerError(){
        self::respondJSON(500, null, false);
    }
    
    public static function respondServiceDown(){
        self::respondJSON(503, null, false);
    }
    
    public static function respondUnauthorised(){
        self::respondJSON(401, null, false);
    }
    
    public static function respondForbidden($data){
        self::respondJSON(403, $data, true);
    }
    
    //request did not pass validation
    public static function respondBadRequest(){
        self::respondJSON(400, null, false);
    }
    
    

    function addSub($sub, callable $function) {
        $this->arrSubDirectory[] = array("name" => $sub, "function" => $function);
    }

    function getInstance() {
        header('Content-Type: application/json');
        $state = false;
        
        
        for ($i = 0; $i <= (sizeof($this->arrSubDirectory) - 1 ); $i++) {

            if (isset($_SERVER["PATH_INFO"]) && ("/" . trim($_SERVER["PATH_INFO"], '/')) == $this->arrSubDirectory[$i]['name']) {

                $this->arrSubDirectory[$i]['function']();
                $state = true;
                
            }
        }
        if (!$state) {
            return false;
        } else {
            
            return true;
        }
    }

    

}

/*

  // ---Sample use---

  $host = new PenguRESTHost();


  //sample directory and function
  $host->addsub("/sub", function(){

  //do somthing
  echo time();

  });


  // N.B.!!!
  $host->getInstance();


 */
?>
