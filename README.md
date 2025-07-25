# arbor

#### Initialization
1. Run [docker-compose.yaml](docker%2Fdocker-compose.yaml) to create database;
2. Use credentials from the [.env](docker%2F.env) to connect locally to datasource;
3. Add properties from the [application-test.yaml](src%2Ftest%2Fresources%2Fapplication-test.yaml) to _src/main/resources/application.yaml_;
   - don't forget to specify _client-id_ and _client-secret_;
4. To create a database schema, build and start the project;
5. Populate table with initial data using [init.sql](src%2Fmain%2Fresources%2Finit.sql).

#### Authorization
Application uses OAuth2.0 with the Google provider, so in order to make requests you should first authorize.

After calling any secured endpoint (i.e., http://localhost:8080/) you will be redirected to Google Account Chooser. 
Select your Google account with the DevTools Network tab opened, and copy _JSESSIONID_ value from the cookies.

Now you can use it for authorized API calls, i.e. add _JSESSIONID_=_copied_value_ as a cookie to Postman.
