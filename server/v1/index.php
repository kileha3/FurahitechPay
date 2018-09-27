<?php
use Slim\Http\Request;
use Slim\Http\Response;

include_once "../vendor/autoload.php";
include_once "../core/FurahitechPay.php";
$configuration = [
    'settings' => [
        'displayErrorDetails' => true,
    ],
];
$c = new \Slim\Container($configuration);
$app = new Slim\App($c);
$checkProxyHeaders = true;
$trustedProxies = [];
$app->add(new RKA\Middleware\IpAddress($checkProxyHeaders, $trustedProxies));

$app->post("/tigo/authenticate",function(Request $request,  Response $response, $args = []){
    $furahitechPay = new FurahitechPay();
    $transactionDetails = $furahitechPay->serializeObject($request->getParsedBody());
    $transactionDetails->gateway = "TIGOPESA";
    $responseData = $furahitechPay->getSecurePaymentRedirectURL($transactionDetails);
    return $response->withJson($responseData,200);
});


$app->post("/vodacom/authenticate",function(Request $request,  Response $response, $args = []){
    $furahitechPay = new FurahitechPay();
    $transactionDetails = $furahitechPay->serializeObject($request->getParsedBody());
    $transactionDetails->gateway = "MPESA";
    $responseData = $furahitechPay->payWithMpesa($transactionDetails);
    return $response->withJson($responseData,200);
});


$app->post("/stripe/authenticate",function(Request $request,  Response $response, $args = []){
    $furahitechPay = new FurahitechPay();
    $transactionDetails = $furahitechPay->serializeObject($request->getParsedBody());
    $responseData = $furahitechPay->payWithStripe($transactionDetails);
    return $response->withJson($responseData,200);
});


$app->post("/tigo/callback",function(Request $request,  Response $response, $args = []){
    $furahitechPay = new FurahitechPay();
    $paymentDetails = $furahitechPay->serializeObject($request->getParsedBody());
    $transactionRef = $paymentDetails->transaction_ref_id;
    $transactionStatus = $paymentDetails->trans_status == "success" ? 200:404;
    $paymentDetails = $furahitechPay->mobilePaymentCallback($transactionRef,$transactionStatus);
    return $response->withJson($paymentDetails,200);
});


$app->post("/vodacom/callback",function(Request $request,  Response $response, $args = []){
    $paymentDetails = json_decode(file_get_contents('php://input'));
    $furahitechPay = new FurahitechPay();
    $fileData = new \stdClass();
    $fileData->timestamp = date("Y-m-d H:i:s")." UTC";
    $fileData->postBodyRaw = $paymentDetails;
    $fileData->urlQueryParams = $request->getQueryParams();
    $fileData->postBodyForm = $request->getBody();
    $fileData->request = $request->getAttributes();
    $furahitechPay->writeContentToAFile($fileData,"vodacomCallbackResult.json");

    if($paymentDetails != null){
        if($furahitechPay->isDataExists($paymentDetails,'code')
            && $furahitechPay->isDataExists($paymentDetails,'uid')){
            $transactionRef = $paymentDetails->transactionRefId;
            $transactionStatus = $paymentDetails->code;
            var_dump($transactionRef);
            $paymentDetails = $furahitechPay->mobilePaymentCallback($transactionRef,$transactionStatus);
        }
    }
    return $response->withJson($paymentDetails,200);
});


$app->delete("/{transactionRef}/acknowledge",function(Request $request,  Response $response, $args = []){
    $furahitechPay = new FurahitechPay();
    $responseData = $furahitechPay->deletePaymentTemFile($args['transactionRef']);
    return $response->withJson($responseData,200);
});

$app->get("/default",function(Request $request,  Response $response, $args = []){
    return $response->withJson("Default redirection...",200);
});

$app->get("/",function(Request $request,  Response $response, $args = []){
    $responseData = new \stdClass();
    $responseData->error = true;
    $responseData->message = "You are not authorized to access this resource";
    return $response->withJson($responseData,200);
});

$app->post("/",function(Request $request,  Response $response, $args = []){
    $responseData = new \stdClass();
    $responseData->error = true;
    $responseData->message = "You are not authorized to access this resource";
    return $response->withJson($responseData,200);
});


try {
    $app->run();
} catch (\Slim\Exception\MethodNotAllowedException $e) {
    var_dump($e->getMessage());
} catch (\Slim\Exception\NotFoundException $e) {
    var_dump($e->getMessage());
} catch (Exception $e) {
    var_dump($e->getMessage());
}