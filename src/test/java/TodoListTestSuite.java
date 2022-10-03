import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import util.TestDataBuild;
import java.io.IOException;
import java.util.List;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class TodoListTestSuite extends TestDataBuild {
    String id;
    String itemDescription = itemNameGen();

    @Test(priority = 1)
    @Step("Create a new item / Verify [status code:200, extract item ID]")
    public void createNewItem() throws IOException {
        Response response = given().spec(requestSpecification())
                .body(addItem(itemDescription))
                .when().post(todoItemEndPoint);
        response.then().log().all().statusCode(201);

        /* Extract the ID for the item created */
        id = response.asString().replaceAll("^\"|\"$", "");
    }

    @Test(priority = 2)
    @Description("Try creating item with duplicate name / Verify [status code:409, error message]")
    public void createItemWithDuplicateItemName() throws IOException {
        Response response = given().spec(requestSpecification())
                .body(addItem(itemDescription))
                .when().post(todoItemEndPoint);

        String duplicateMessage =response.then().log().all().statusCode(409).extract().response().asString();
        Assert.assertEquals(duplicateMessage,"A todo item with description already exists");
    }

    @Test(priority = 3)
    @Description("get complete item list / Verify [status code:200, the newly created item should be listed along with its ID]")
    public void getCompleteItemList() throws IOException {
        Response response = given().spec(requestSpecification())
                .when().get(todoItemEndPoint);
        response.then().log().all().statusCode(200);

        /* Get the list of items as json objects */
        JsonPath jsonPath = new JsonPath(response.asString());
        List<String> descList = jsonPath.getList("description");

        /* Verify the newly created item is listed along with its ID */
        for(int i=0;i< descList.size();i++){
            if (descList.get(i).contains(itemDescription)){
                String associatedID = jsonPath.getString("id["+i+"]");
                Assert.assertEquals(associatedID,id);
            }
        }
    }

    @Test(priority = 4)
    @Description("Mark the status of the item as completed / Verify [Response time, status code:204]")
    public void editItemAsCompleted() throws IOException {
        Response response = given().spec(requestSpecification()).body(editItem(itemDescription,id))
                .when().put(todoItemEndPoint +id);

        response.then().log().all().assertThat().
                statusCode(204).
                time(Matchers.lessThan(1000L));            /*Verify the Response time*/
    }

    @Test(priority = 5)
    @Description("Try editing item with invalid description / Verify [status code:400]")
    public void editItemWithInvalidDescription() throws IOException {
        Response response = given().spec(requestSpecification()).body(editItem("",id))
                .when().put(todoItemEndPoint +id);

        response.then().log().all().assertThat().statusCode(400);
    }

    @Test(priority = 6)
    @Description("Ensure item is completed / Verify [Json schema, Response time, status code:200, isCompleted: true, Response header]")
    public void getItemSpecificDetail() throws IOException {
        Response response = given().spec(requestSpecification())
                .when().get(todoItemEndPoint + id);

        //Verify the GET call is successful
        response.then().assertThat().
                statusCode(200).
                body(matchesJsonSchemaInClasspath("getItemDetailSchema.json")).               /*Verify the JSON SCHEMA*/
                time(Matchers.lessThan(1000L));                                                              /*Verify the Response time*/
        Assert.assertEquals(new JsonPath(response.asString()).getString("isCompleted"),"true");
        Assert.assertEquals(response.header("Content-Type"), "application/json; charset=utf-8" );
    }

    @Test(priority = 7)
    @Description("Try to get item detail with invalid item ID / Verify [status code:404, error message]")
    public void getItemDetailWithInvalidItemID() throws IOException {
        Response response = given().spec(requestSpecification())
                .when().get(todoItemEndPoint + getProperty("invalidItemID"));

        String notFoundMessage =response.then().log().all().statusCode(404).extract().response().asString();
        Assert.assertEquals(notFoundMessage,"Todo item with id "+getProperty("invalidItemID")+" not found");
    }
}
