<?php



class PenguEncrypt{
    
    const ENCRYPTION_METHOD = 'aes-256-cbc';
    const MULTI_FACTOR_ENCRYPTION_PATTERN = '$salt$iv$hmac$encypted';
    const ENCRYPTION_PATTERN = '$salt$iv$hmac$encypted$hmacsalt';
    const SERVER_PEPPER = '2e11b4a047a8238a8f335e';
    
    public static function generateSecretKey(){
        return base64_encode(openssl_random_pseudo_bytes(16));
    }
    
    public static function create_combinedkey($input){
            $temp = password_hash($input, PASSWORD_BCRYPT, ['salt'=> self::SERVER_PEPPER]);
            
            return substr(explode('$',$temp )[3],22);
    }
    
    public static function multiFactorEncrypt($key1, $key2, $data){
        $secured_key = password_hash($key1.$key2,PASSWORD_BCRYPT, ['cost'=>11]);
        $salt = substr(explode('$', $secured_key)[3],0,22);
        
        
        $secured_key2 = base64_decode($key2);
        
        $iv = openssl_random_pseudo_bytes(openssl_cipher_iv_length(self::ENCRYPTION_METHOD));
        
        $encrypted1 = openssl_encrypt($data, self::ENCRYPTION_METHOD, $secured_key, OPENSSL_RAW_DATA, $iv);
        $encrypted2 = hash_hmac('sha3-512', $encrypted1, $secured_key2, TRUE);
        
        $output = '$'.$salt.'$'.base64_encode($iv).'$'. base64_encode($encrypted2).'$'. base64_encode($encrypted1);
        
        return $output;
    }
    
    public static function encrypt($key, $data){
        $secured_key = password_hash($key,PASSWORD_BCRYPT, ['cost'=>11]);
        $salt = substr(explode('$', $secured_key)[3],0,22);
        $iv = openssl_random_pseudo_bytes(openssl_cipher_iv_length(self::ENCRYPTION_METHOD));
        $secured_key2 = self::generateSecretKey();
        
        $encrypted1 = openssl_encrypt($data, self::ENCRYPTION_METHOD, $secured_key, OPENSSL_RAW_DATA, $iv);
        $encrypted2 = hash_hmac('sha3-512', $encrypted1, $secured_key2, TRUE);
        
        $output = '$'.$salt.'$'.base64_encode($iv).'$'. base64_encode($encrypted2).'$'. base64_encode($encrypted1).'$'. base64_encode($secured_key2);
        
        return $output;
    }
    
    public static function multiFactorDecrypt($key1,$key2,$encrypted){
        $exploded = explode("$", $encrypted);
        $salt = $exploded[1];
        $iv = base64_decode($exploded[2]);
        $encrypted2 = base64_decode($exploded[3]);
        $encrypted1 = base64_decode($exploded[4]);
        
        $secured_key = password_hash($key1.$key2, PASSWORD_BCRYPT,['cost'=> 11, 'salt'=> $salt]);
        
        $secured_key2 = base64_decode($key2);
        
        $data = openssl_decrypt($encrypted1, self::ENCRYPTION_METHOD, $secured_key, OPENSSL_RAW_DATA, $iv);
        
        $encrypted2new = hash_hmac('sha3-512', $encrypted1, $secured_key2, TRUE);
        
        if(hash_equals($encrypted2, $encrypted2new))
            return $data;
        
        return false;
        
    }
    
    public static function  decrypt($key, $encrypted){
        $exploded = explode("$", $encrypted);
        $salt = $exploded[1];
        $iv = base64_decode($exploded[2]);
        $encrypted2 = base64_decode($exploded[3]);
        $encrypted1 = base64_decode($exploded[4]);
        
        
        $secured_key = password_hash($key, PASSWORD_BCRYPT,['cost'=> 11, 'salt'=> $salt]);
        
        $secured_key2 = base64_decode($exploded[5]);
        
        $data = openssl_decrypt($encrypted1, self::ENCRYPTION_METHOD, $secured_key, OPENSSL_RAW_DATA, $iv);
        
        $encrypted2new = hash_hmac('sha3-512', $encrypted1, $secured_key2, TRUE);
        
        if(hash_equals($encrypted2, $encrypted2new))
            return $data;
        
        return false;
    }
        
}
