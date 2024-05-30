# Example Token Server Using JAVA

#TEXT COPIED FROM PYTHON VERSION

## Endpoints

- GET `/start`: Call Incode's `/omni/start` API to create an Incode session which will include a token in the JSON response.  This token can be shared with Incode SDK client apps to do token based initialization, which is a best practice.

- GET `/onboarding-url`: Calls incodes `/omni/start` and then with the token calls `/0/omni/onboarding-url` to retrieve the unique onboarding-url for the newly created session.

- GET `/fetch-score` : Calls incodes `/omni/get/score` to retrieve the overall score of a finished onboarding session. Requires a valid interviewId as request param and the session token as request header.

It can receive the optional query parameter `redirectUrl` to set where to redirect the user at the end of the flow.

- POST `/webhook`: Example webhook that reads the json data and return it back a response, from here you could fetch scores or OCR data when the status is ONBOARDING_FINISHED

## Secure Credential Handling
We highly recommend to follow the 0 rule for your implementations, where all sensitive calls to incode's endpoints are done in the backend, keeping your apikey protected and just returning a `token` with the user session to the frontend.

Within this sample you will find the only call to a `/omni/` endpoint we recommend for you to have, it requires the usage of the `apikey`, all further calls must be done using only the generated `token` and be addresed to the `/0/omni` endpoints. 

## Prerequisites
This sample requires [JAVA 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) with at least [gradle 7.3+](https://gradle.org/install/) installed.

Gradle 7.3 is the first version that fully supports Java 17. Newer versions are also compatible with Java 17.
## Local Development

### Environment
Rename `application.properties.example` file to `application.properties` and add your api url, api key, clientId, and flowId details:

```application.properties
API_URL=https://demo-api.incodesmile.com
API_KEY=yourapikey
CLIENT_ID=yourclientid
FLOW_ID=yourflowid
ADMIN_TOKEN=Needed for the webhooks and the polling calls from the frontend to be able to fetch Scores and auto-approve
server.port=3000
```

### Run Localy
Using gradle to install all the dependencies.
```bash
gradle build
```

Then start the local server with.
```bash
gradle bootRun
```

Or run it directly through tour preferred IDE.

The server will accept petitions on `http://localhost:3000/`.

You can change the server port on the `application.properties` file but remember which port your frontend is accessing.

### Expose the server to the internet for frontend testing with ngrok
For your frontend to properly work in tandem with this server on your mobile phone for testing, you will need a public url with proper SSL configured, by far the easiest way to acchieve this with an ngrok account properly configured on your computer. You can visit `https://ngrok.com` to make a free account and do a quick setup.

In another shell expose the server to internet through your computer ngrok account:

```bash
ngrok http 3000
```

Open the `Forwarding` adress in a web browser. The URL should look similar to this: `https://466c-47-152-68-211.ngrok-free.app`.

Now you should be able to visit the following routes to receive the associated payloads:
1. `https://yourforwardingurl.app/start`
2. `https://yourforwardingurl.app/onboarding-url`
3. `https://yourforwardingurl.app/onboarding-url?redirectionUrl=https%3A%2F%2Fexample.com%2F`
4. `https://yourforwardingurl.app/fetch-score?interviewId=660ee696a9b89db96502d8db`

## Webhook
We provide an example on how to read the data we send in the webhook calls, from here you could
fetch scores and OCR data, what you do with that is up to you.

To recreate the call and the format of the data sent by Incode you can use the following script:

```bash
curl --location 'https://yourforwardingurl.app/webhook' \
--header 'Content-Type: application/json' \
--data '{
    "interviewId": "<interviewId>",
    "onboardingStatus": "ONBOARDING_FINISHED",
    "clientId": "<clientId>",
    "flowId": "<flowId>"
}'
```

## Dependencies

* **Java17**: Java is a high-level, general-purpose programming language.
* **gradle7.3+**: Used to access external dependencies, and to build and run the project.
* **ngrok**: Unified ingress platform used to expose your local server to the internet.
