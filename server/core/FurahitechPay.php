<?php

/**
 * Class which handles all operations like requesting authorization,
 * generating payment reference code and many other.
 */
class FurahitechPay{

    private $environment = false;

    private $apiType;

    private $requestResponse;

    private $messageGroup;

    private $logDirPath;

    private $oneSignalApiKey;

    private $oneSignalAppId;

    /**
     * FurahitechPay constructor.
     */
    function __construct(){
        require_once "FurahitechConstant.php";
        require_once "../vendor/autoload.php";
        $this->apiType = 0;
        $this->requestResponse = new \stdClass();
        $this->messageGroup = "PaymentAPI";
        $this->logDirPath = "../logs/";
    }

    /**
     * Serialize any object to a real object
     * @param $object mixed to serialize
     * @return mixed Serialized object
     */
    function serializeObject($object){
        return json_decode(json_encode($object));
    }

    /**
     * Check if an object contain a certain key
     * @param $dataSource array data source
     * @param $tag string Key to check for
     * @return bool TRUE if exist else FALSE
     */
    function isDataExists($dataSource,$tag){
        return array_key_exists($tag,$dataSource);
    }

    /**
     * Write content to the file
     * @param $contentToWrite mixed content ot be written to the file
     * @param $fileToWriteTo string file path to write the content
     * @return bool|int TRUE if written otherwise FALSE
     */
    function writeContentToAFile($contentToWrite, $fileToWriteTo){
        return file_put_contents($fileToWriteTo,json_encode($contentToWrite));
    }

    /**
     * Read content from the file
     * @param $fileToReadFrom string file path to read from
     * @return string Read file content
     */
    function readContentFromTheFile($fileToReadFrom){
        return file_get_contents($fileToReadFrom,true);
    }


    /**
     * Send push message to the device
     * @param $emailAddress string client's email address
     * @param $notificationType : Type of notification to be sent
     * @param $backgroundMode TRUE for background notification otherwise false
     * @param $notificationAction : Action to be taken
     * @param $notificationData : Payload data to be passed to the device
     * @param $notificationTitle : Notification title
     * @param $notificationMessage : Notification message
     * @return mixed Notification response
     */
    private function sendPushMessage($emailAddress,$notificationType, $backgroundMode, $notificationAction,
                             $notificationData, $notificationTitle, $notificationMessage){
        $this->apiType = 2;
        $this->environment = true;
        $content = array(
            "en" => $notificationMessage
        );

        $heading = array(
            "en" => $notificationTitle
        );

        $fields = null;
        $requestBody = array(
            'app_id' => $this->oneSignalAppId,
            'data' => array(
                "notification_action" => $notificationAction,
                "notification_data" => $notificationData,
                "notification_mode" => $backgroundMode,
                "notification_type" => $notificationType
            ),
            'filters' => array(
                array("field" => "tag", "key" => "clientId", "relation" => "=", "value" => "$emailAddress")
            ),
            'contents' => $content,
            'android_group' => $this->messageGroup,
            'headings' => $heading
        );

        $headers = array(
            'Content-Type: application/json; charset=utf-8',
            'Authorization: Basic '.$this->oneSignalApiKey
        );
        return  $this->executePostRequest($headers,json_encode($requestBody),ENVIRONMENT_ONE_SIGNAL);


    }


    /**
     * Execute POST on specific URL with specified request header and body
     * @param $requestHeaders mixed Request header data
     * @param $requestBody mixed Request body data
     * @param $requestEndPoint string URL to be executed
     * @return mixed Response of the POSt request
     */
    function executePostRequest($requestHeaders, $requestBody, $requestEndPoint){
        $requestURL = ($this->environment ? ($this->apiType == 0 ? ENVIRONMENT_TIGO_LIVE :
                    ($this->apiType == 1 ? ENVIRONMENT_WAZO_LIVE:"") )
                :ENVIRONMENT_TIGO_SANDBOX).$requestEndPoint;
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $requestURL);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_POST, 1);
        if($requestHeaders != null){
            curl_setopt($ch, CURLOPT_HTTPHEADER, $requestHeaders);
        }
        if($requestBody != null){
            curl_setopt($ch,CURLOPT_POSTFIELDS,$requestBody);
        }

        $result = curl_exec($ch);
        if (curl_errno($ch)) {
            var_dump('Error:' . curl_error($ch));
        }
        curl_close ($ch);
        return $result;
    }

    /**
     * Get secure session ID from Tigo Pesa servers
     *
     * @return string acquired session Id
     */
    private function getTigoPesaSessionId(){
        $headers = array();
        $request_url = "oauth/generate/accesstoken?grant_type=client_credentials";
        $headers["Content-Type"]="application/x-www-form-urlencoded";
        $requestBody="client_id=".TIGO_MERCHANT_ACCOUNT_KEY."&client_secret=".TIGO_MERCHANT_ACCOUNT_SECRET;
        $response = $this->executePostRequest($headers,$requestBody,$request_url);
        return json_decode($response)->accessToken;
    }

    /**
     * Get unique transaction reference code
     * @param $length int length of the transaction reference code
     * @return string Generated transaction reference code
     */
    private function generateTransactionRefCode($length){
        srand((double) microtime(TRUE) * 1000000);
        $randomAlphaNumeric = "";
        $chars = array(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

        for ($rand = 0; $rand <= $length; $rand++) {
            $random = rand(0, count($chars) - 1);
            $randomAlphaNumeric .= $chars[$random];
        }
        return $randomAlphaNumeric;
    }

    /**
     * Get payment request body
     * @param $transactionDetails object User request details
     * @return mixed constructed payment request body
     */
    private function getPaymentRequestBody($transactionDetails){
        $requestBody['MasterMerchant'] = array(
            'account' => TIGO_MERCHANT_ACCOUNT_MSISDN,
            'pin' => TIGO_MERCHANT_ACCOUNT_PIN,
            'id' => TIGO_MERCHANT_ACCOUNT_NAME
        );

        $requestBody['Subscriber'] = array(
            'account' => $transactionDetails->phoneNumber,
            'countryCode' => TRANSACTION_COUNTRY_CODE,
            'country' => TRANSACTION_COUNTRY_NAME,
            'firstName' => $transactionDetails->firstName,
            'lastName' => $transactionDetails->lastName,
            'emailId' => $transactionDetails->emailAddress
        );

        $requestBody['redirectUri'] = TRANSACTION_URL_REDIRECTION;
        $requestBody['callbackUri'] = TRANSACTION_URL_CALLBACK;
        $requestBody['language'] = TRANSACTION_LANGUAGE;

        $requestBody['originPayment']=array(
            'amount' => $transactionDetails->amount,
            'currencyCode' => TRANSACTION_COUNTRY_CURRENCY,
            'tax' => $transactionDetails->taxAmount,
            'fee' => $transactionDetails->feeAmount
        );

        $requestBody['LocalPayment'] = array(
            'amount' => $transactionDetails->amount,
            'currencyCode' => TRANSACTION_COUNTRY_CURRENCY
        );
        $requestBody['transactionRefId'] = $this->generateTransactionRefCode(16);
        return $requestBody;
    }

    /**
     * Get secure page redirection details
     * @param $transactionDetails object user request params
     * @return mixed Secure page redirection details
     */
    function getSecurePaymentRedirectURL($transactionDetails){
        $this->environment = $transactionDetails->environment;
        $this->apiType = 0;
        $requestUrl = "tigo/payment-auth/authorize";
        $headers = array(
            'Content-Type: application/json',
            'accessToken:'.$this->getTigoPesaSessionId()
        );
        $requestBody = $this->getPaymentRequestBody($transactionDetails);
        $requestBody = json_encode($requestBody);
        $response = json_decode($this->executePostRequest($headers,$requestBody,$requestUrl));
        $this->requestResponse->data = $response->redirectUrl;
        $this->requestResponse->transactionRef = $response->transactionRefId;
        $filePath = $this->logDirPath.$this->requestResponse->transactionRef.".json";
        $contentWritten = $this->writeContentToAFile($transactionDetails,$filePath);
        return $contentWritten ? $this->requestResponse:null;
    }


     private function getWazoHubAuthToken(){
        $requestBody = array(
            'grant_type' => WAZO_MERCHANT_GRANT_TYPE,
            'client_id' => WAZO_MERCHANT_ID,
            'scope' => WAZO_MERCHANT_SCOPE,
            'client_secret' => WAZO_MERCHANT_SECRET
        );
        $requestUrl = "api/v1/access_token";
        return json_decode($this->executePostRequest(null,$requestBody,$requestUrl))->access_token;
    }

    /**
     * Start payment with Mpesa through WazoHub
     * @param $transactionDetails object user request params
     * @return stdClass
     */
    function payWithMpesa($transactionDetails){
        $this->environment = $transactionDetails->environment;
        $this->apiType = 1;
        $requestUrl = "api/v1/c2b/push/mpesa";
        $requestBody = array(
          'msisdn' => $transactionDetails->phoneNumber,
          'amount' => $transactionDetails->amount,
          'operator' => WAZO_OPERATOR_NAME,
          'language' => TRANSACTION_LANGUAGE_WAZO,
          'requestId' => $this->generateTransactionRefCode(16)
        );
        $headers = array(
            'Content-Type: application/json',
            'Authorization: Bearer '.$this->getWazoHubAuthToken()
        );
        $requestBody = json_encode($requestBody);
        $response =  json_decode($this->executePostRequest($headers,$requestBody,$requestUrl));
        $this->requestResponse->data = $response->requestId;
        $this->requestResponse->transactionRef = $response->uid;
        $filePath = $this->logDirPath.$this->requestResponse->transactionRef.".json";
        $contentWritten = $this->writeContentToAFile($transactionDetails,$filePath);
        return $contentWritten ? $this->requestResponse: null;
    }


    /**
     * Pay using stripe gateway for card
     * @param $transactionDetails
     * @return stdClass
     */
    function payWithStripe($transactionDetails){
        $this->oneSignalApiKey = $transactionDetails->oneSignalApiKey;
        $this->oneSignalAppId = $transactionDetails->oneSignalAppId;
        \Stripe\Stripe::setApiKey(STRIPE_SECRET_API_KEY);

        $customer = \Stripe\Customer::create(array(
                "source" => $transactionDetails->cardToken,
                "email"=>$transactionDetails->emailaddress,
                "description" =>$transactionDetails->description)
        );
        $card_charge =\Stripe\Charge::create(array(
            'amount' => $transactionDetails->amount."00",
            'currency' => TRANSACTION_COUNTRY_CURRENCY,
            'customer' => $customer->id));
        $responseData=str_replace('Stripe\Charge JSON:','',$card_charge);
        $data = json_decode($responseData);
        $notificationData = new \stdClass();

        $notificationData->status = $data->outcome->network_status == "approved_by_network";
        $notificationData->productName = $transactionDetails->productName;
        $notificationData->productId = $transactionDetails->productId;
        $notificationData->transactionRef = $data->id;
        $notificationData->gateway = $data->source->brand;
        $notificationData->amount = $transactionDetails->amount;
        $notificationData->url = $transactionDetails->dataReceiver;
        $notificationData->timeStamp = strtotime(gmdate("Y-m-d H:i:s"));

        $code = substr($notificationData->transactionRef,10,strlen($notificationData->transactionRef));

        $message = $data->outcome->network_status == "approved_by_network" ?
            "Thank you, you have successfully paid $transactionDetails->amount TZS. Your payment confirmation code is $code. <br/><br/>Processed by ":
            "Unfortunately, your payment was not successfully processed by ";
        $message = $message."<b>FurahitechPay</b>";
        $notificationData->message = $message;

        $this->handleNotification($transactionDetails->emailAddress,$message,$notificationData);

        $this->requestResponse->data = $data->outcome->seller_message;
        $this->requestResponse->transactionRef = $data->id;
        return $this->requestResponse;
    }

    /**
     * Delete the tem file logged during payment process
     * @param $transactionRef string Payment transaction reference id
     * @return stdClass
     */
    function deletePaymentTemFile($transactionRef){
        $filePath = $this->logDirPath.$transactionRef.".json";
        $deleted = unlink($filePath);
        $message = $deleted ? $transactionRef." deleted successfully" :
            "Failed to delete the tem file";
        $this->requestResponse->transactionRef = $transactionRef;
        $this->requestResponse->data = "Acknowledged: ".$message;
        return $this->requestResponse;
    }

    /**
     * Handle notifying respective user for their transaction results
     * @param $emailAddress string Client email address
     * @param $message string Message to be broadcasted
     * @param $notificationData mixed Set of transaction data to be sent to the client
     * @return mixed|null
     */
    private function handleNotification($emailAddress,$message,$notificationData){
        $this->environment = true;
        $this->apiType = 2;

        if($notificationData->url != "null" && $notificationData->status){
            $this->requestResponse = $this->executePostRequest(null,$notificationData,$notificationData->url);
        }

        if($this->requestResponse != null){
           return $this->sendPushMessage($emailAddress,"paymentApi",true,
               "new", $notificationData,"Payment Feedback",$message);
        }
        return null;
    }

    /**
     * Notify the user after receiving callback from the MNO
     * @param $transactionRef string Transaction reference Id
     * @param $transactionStatus
     * @return stdClass
     */
    function mobilePaymentCallback($transactionRef, $transactionStatus){
        $filePath = $this->logDirPath.$transactionRef.".json";
        $clientInfo = json_decode($this->readContentFromTheFile($filePath));
        $this->oneSignalApiKey = $clientInfo->oneSignalApiKey;
        $this->oneSignalAppId = $clientInfo->oneSignalAppId;
        if($clientInfo != null){
            $message = $transactionStatus == TRANSACTION_FAILURE ?
                "Thank you, you have successfully paid $clientInfo->amount TZS. Your payment confirmation code is $transactionRef.
             <br/><br/>Processed by ":"Unfortunately, your payment was not successfully processed by ";
            $message = $message."<b>FurahitechPay</b>";
            $notificationData = new \stdClass();
            $notificationData->status = $transactionStatus == TRANSACTION_SUCCESS;
            $notificationData->productName = $clientInfo->productName;
            $notificationData->productId = $clientInfo->productId;
            $notificationData->message = $message;
            $notificationData->transactionRef = $transactionRef;
            $notificationData->timeStamp = strtotime(gmdate("Y-m-d H:i:s"));
            $notificationData->amount = $clientInfo->amount;
            $notificationData->url = $clientInfo->dataReceiver;
            $this->handleNotification($clientInfo->emailAddress,$message,$notificationData);
            $notification = $notificationData;
            $this->requestResponse->error = !($clientInfo != null && $transactionStatus == TRANSACTION_SUCCESS);
            $this->requestResponse->message = $message;
            $this->requestResponse->data = $notification;
            return $this->requestResponse;
        }

        return null;
    }
}