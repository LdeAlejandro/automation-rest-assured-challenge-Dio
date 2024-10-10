import Entities.Booking;
import Entities.BookingDates;
import Entities.TokenUser;
import Entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Test by order

public class BookingTests {
    public static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;
    private static TokenUser tokenuser;
    private static String AuthToken;
    public static String TestBookingId ="";


    @BeforeAll
    public static void Setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8,10),
                faker.phoneNumber().toString());

        user.setAdditionalNeeds("Lunch");

        bookingDates = new BookingDates("2018-01-02", "2018-01-03");
        booking = new Booking(user.getFirstName(), user.getLastName(),
                (int)faker.number().randomDouble(2, 50, 100000),
                true,bookingDates,
                "");
        RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter(), new ErrorLoggingFilter());

        AuthToken = booking.getAuthtoken();
    }

    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON);
                //.auth().basic("admin", "password123");
    }

    @Test
    @Order(1)
    public void PingHealthCheck_201OK() {

        given()
                .config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .when()
                .get("/ping/")
                .then()
                .and()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.TEXT)
                .and()
                .time(lessThan(2000L));
    }

    @Test
    public void CreateToken_OK() {

        TokenUser testAdminUser = new TokenUser();
        System.out.println("**************** TokenUser: " + testAdminUser.getUserName() + testAdminUser.getPassword() +"\n***************");

        Response response = request
                .when()
                .body(testAdminUser)
                .post("/auth/")
                .then()
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .and()
                .time(lessThan(2000L))
                .extract()
                .response();
        System.out.println("**************** Reponse: " + response+"\n***************");
    }




    @Test
    public void getAllBookingsById_returnOk(){
        Response response = request
                .when()
                .get("/booking")
                .then()
                .extract()
                .response();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }

    @Test

    public void  getAllBookingsByUserFirstName_BookingExists_returnOk(){
        request
                .when()
                .queryParam("firstname", "John")
                .get("/booking")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .and()
                .body("results", hasSize(greaterThan(0)));

    }

    @Test
    public void  getAllBookingsByFirstAndLastName_BookingExists_returnOk(){
        request
                .when()
                .queryParam("firstname", "Josh")
                .queryParam("lastname", "Allen")
                .get("/booking")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .and()
                .body("results", hasSize(greaterThan(0)));
    }

    @Test
    @Order(2)
    public void  CreateBooking_WithValidData_returnOk(){

        Booking test = booking;
        Response response =
                given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                        .contentType(ContentType.JSON)
                        .when()
                        .body(test)
                        .post("/booking/")
                        .then()
                        .body(matchesJsonSchemaInClasspath("createBookingRequestSchema.json"))
                        .and()
                        .assertThat()
                        .statusCode(200)
                        .contentType(ContentType.JSON).and().time(lessThan(2000L))
                        .extract()
                        .response();
        //extract booking id Created and assign it to booking id test variable
        JsonPath jsonPath = new JsonPath(response.asString());
        TestBookingId = Integer.toString(jsonPath.getInt("bookingid"));
        System.out.println("******************Booking id: " + TestBookingId);

    }

    @Test
    @Order(3)
    public void  getBookingById_Booking_Exist_returnOk(){

        Booking test = booking;

        Response response = request
                .when()
                .get("/booking/"+ TestBookingId)
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .and()
                .extract()
                .response();
        System.out.println("*******************booking id: "+ TestBookingId);
        Assertions.assertEquals(test.getFirstname(), response.jsonPath().getString("firstname"));
        Assertions.assertEquals(test.getLastname(), response.jsonPath().getString("lastname"));
        Assertions.assertEquals(test.getTotalprice(), response.jsonPath().getInt("totalprice"));
        Assertions.assertTrue(response.jsonPath().getBoolean("depositpaid"));
    }

    @Test
    @Order(4)
    public void  UpdateBooking_returnOk(){

        Booking test = booking;
        System.out.println("******************put: " + "/booking/"+TestBookingId);
        given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .header("Authorization", AuthToken)
                .when()
                //update data
                .body(test)
                .put("/booking/"+TestBookingId)
                .then()
                .log().all()
                .body(matchesJsonSchemaInClasspath("updateBookingRequestSchema.json"))
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON).and().time(lessThan(2000L));
    }

    @Test
    @Order(5)
    public void PartialUpdateBooking_returnOk() {
        Booking data = booking;
        // Json to partial update
        String test = String.format("{\"booking\": {\"firstname\": \"%s\", \"lastname\": \"%s\"}}",
                data.getFirstname(), data.getLastname());

        // Send the PATCH request
        given()
                .config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .header("Authorization", AuthToken)
                .when()
                .body(test) // Use the updated JSON string
                .patch("/booking/"+TestBookingId)
                .then()
                .log().all()
                .body(matchesJsonSchemaInClasspath("partialUpdateBookingRequestSchema.json")) // Validate against the schema
                .and()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .and()
                .time(lessThan(2000L));
    }

    @Test
    @Order(6)
    public void DeleteBookingById_returnOk() {

        given()
                .config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .header("Authorization", AuthToken)
                .when()
                .delete("/booking/"+TestBookingId)
                .then()
                .and()
                .assertThat()
                .statusCode(201)
                .contentType(ContentType.TEXT)
                .and()
                .time(lessThan(2000L));
    }

}