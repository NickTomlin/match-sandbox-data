match-sandbox-data
---

This is a repo to help troubleshoot some issues I am having with the MasterCard match [sandbox data](https://developer.mastercard.com/documentation/match/#sandbox-data). Namely, I _think_ I am sending the right test data to receive a terminated principal match but I am not receiving one in the response.

Setup
---

The example expects the `consumerKey` and `privateKey` to live at `/tmp/match-client-id` and `/tmp/match-private.key`.

Tests
---

I'm really bad at Java so you can either run the `HelloMatchTest` from within Intellji or point junit to `com.example.hellomatch.HelloMatchTest,myTest`
