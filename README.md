# FurahitechPay
<p>
This is a collection of different payment API's, this API supports Stripe, Tigo Pesa and M-Pesa payments for now. For M-Pesa payments we have used wazoHub as our aggregator.
</p>

## Releases <a name="releases"></a>
[![](https://jitpack.io/v/kileha3/FurahitechPay.svg)](https://jitpack.io/#kileha3/FurahitechPay)





## Features <a name="features"></a>
* Small library
* Supports Stripe,M-Pesa and Tigo Pesa payments
* Select only supported mobile network operators on mobile payments
* Automatically monitor for the callback on the server side (M-Pesa payments)
* Operate on both LIVE and SANBOX environments.
* Check payment status from the Tigo Secure page
* Payment instant callback from your back end
* Elegant UI

## Installation <a name="installation"></a>
This API depends on server side logic which helps to log partial payments to a JSON file waiting for a callback from M-Pesa servers and payment authorisation from Tigo Pesa servers.

### Server side installation

This API work with FurahitechPayments-Server repo, what you have to do is:-<br/>
* Clone this repository and upload /server directory to your server
* Submit your callback URL to WazoHub as:-

```html
 <exampledomain.com>/v1/vodacom/callback
```


### Client Side installation (Android)
Add this to your dependencies (Check for the latest release):
```groovy

implementation 'com.github.kileha3:FurahitechPay:1.0'

```

Create a payment request
```java
 PaymentDataRequest request = new PaymentDataRequest();
 request.setAmount(50000);
 request.setEmailAddress("exampleclient@example.com");
 request.setFirstName("John");
 request.setLastName("Doe");
 request.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry");
 request.setProductId("2");
 request.setProductName("Lorem Ipsum");
 request.setGatewayType(Furahitech.GateWay.MNO);
 request.useLiveApi(true);
 request.setDataReceiver("");
 request.setOneSignalApiKey("");
 request.setOneSignalAppId("");
```

Card / Mobile payment request type
```java
 //Mobile payment
 request.setGatewayType(Furahitech.GateWay.MNO);
 
 //Card payment
 request.setGatewayType(Furahitech.GateWay.CARD);
 
```

Send payment request
```java
 FurahitechPay.getInstance()
    .with(this)
    .setBaseUrl("")
    .setPaymentDataRequest(request)
    .setStripePublishableKey("")
    .setPaymentResultListener(this)
    .setSupportedGateway(Furahitech.GateWay.TIGOPESA)
 
```

Make sure your Activity or Fragment implements PaymentResultListener
```java
 public class MainActivity extends AppCompatActivity implements PaymentResultListener
 
```

Override respective methods to listen for the payment status
```java
 
 @Override
 public void onPaymentCompleted(PaymentResult paymentResult) {
    
 }
     
```

Acknowledge to your back-end that you have received payment status
```java

FurahitechPay.getInstance().acknowledgePaymentResult(paymentResult);
     
```


In case you don't fully support all mobile payments just pass supported ones.
```java
...
//Remove the one you don't support from the param list
.setSupportedGateway(TIGOPESA,MPESA)
...
```

For SANDBOX development environment
```java
... 
//Default value is FALSE - which means SANDBOX
request.useLiveApi(true);
...
```
If you want to send payment result
 automatically to your remote server make sure to include its URL on request. Your receiving end must be of type POST
```java
... 
 request.setDataReceiver("");
...
```

POST data which will be sent to your remote URL
```json
{
  "status":true ,
  "productName":"Example Product Name",
  "productId":"12",
  "message":"Unfortunately, your payment was not successfully processed by FurahitechPay<\/b>",
  "transactionRef":"xujJvV7jjHYYV9E79",
  "timeStamp":1537975151,
  "amount":"50000",
  "clientEmail":"example@example.com",
  "clientName":"John Doe"
}

/* status":true = SUCESS */


```
## Contributing <a name="contribute"></a>
* Do you have a new feature in mind?
* Do you know how to improve existing docs or code?
* Have you found a bug?

Feel free to fork this project and send pull request, your contribution will be highly appreciated.Be ready to discuss your code and design logics

## License <a name="license"></a>

    Copyright (c) 2018 Lukundo Kileha

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sub license, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANT ABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.


