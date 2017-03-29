package services.languageProcessor;

import com.fasterxml.jackson.databind.JsonNode;
import services.models.BotResponse;
import play.Configuration;
import play.api.Play;
import play.libs.Json;

import services.queryHandler.Extractor;
import dataAccess.QuestionsDB;

import java.lang.reflect.*;
import play.db.Database;
import play.db.Databases;
public class TaskMap {
  private Configuration configuration = Play.current().injector().instanceOf(Configuration.class);

  /**
   * This method calls appropriate method on run time based on the
   * parameters (methodName and argName) passed and returns a value
   * returned by that method.
   * @param methodName
   * @param issueKey
   * @return
   */

  public static JsonNode questionMapping(String methodName, String issueKey, JsonNode responseBody) {

    TaskMap taskMap = new TaskMap();

    try {

      //call the method at runtime according to the argument "methodName"
      Method method = TaskMap.class.getMethod(methodName, String.class, JsonNode.class);

      JsonNode returnVal = (JsonNode) method.invoke(taskMap, issueKey, responseBody);
      return returnVal;

    } catch (NoSuchMethodException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    } catch (InvocationTargetException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    } catch (IllegalAccessException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    } catch (NullPointerException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    }
  }

  public static JsonNode questionMapping(String methodName) {

    TaskMap taskMap = new TaskMap();

    try {

      //call the method at runtime according to the argument "methodName"
      Method method = TaskMap.class.getMethod(methodName);
      JsonNode returnVal = (JsonNode) method.invoke(taskMap);
      return returnVal;

    } catch (NoSuchMethodException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    } catch (InvocationTargetException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    } catch (IllegalAccessException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    } catch (NullPointerException e) {
      return taskMap.parseToJson("fail", e.getMessage());
    }
  }

  /**
   * This method requests issue info and returns it to the calling method
   *
   * @param issueKey
   * @return
   */
  public JsonNode getTicketDescription(String issueKey, JsonNode responseBody) {
    if (Extractor.extractString(responseBody, "description").equals("[\"Issue Does Not Exist\"]")) {
      return parseToJson("fail", configuration.getString("error-message.issue-not-found"));
    } else {
      String IssueId = responseBody.get("key").toString().replaceAll("\"", "");
      String IssueUrl = "http://jira.agiledigital.com.au/browse/" + IssueId;
      String Hyperlink = "<" + IssueUrl+ "|" + IssueId + ">";

      String answer = "Description of " + Hyperlink + " is as follows: \n" +
        Extractor.extractString(responseBody, "description");
      return parseToJson("success", answer);
    }

  }

  /**
   * This method requests assignee of issue and returns it to the calling method
   *
   * @param issueKey
   * @return
   */
  public JsonNode getTicketAssignee(String issueKey, JsonNode responseBody) {
    if (Extractor.extractString(responseBody, "assignee").equals("[\"Issue Does Not Exist\"]")) {
      return parseToJson("fail", configuration.getString("error-message.issue-not-found"));
    } else {
      String IssueId = responseBody.get("key").toString().replaceAll("\"", "");
      String IssueUrl = "http://jira.agiledigital.com.au/browse/" + IssueId;
      String Hyperlink = "<" + IssueUrl+ "|" + IssueId + ">";

      String answer = Extractor.extractString(responseBody, "assignee") + " is working on " + Hyperlink + ".";
      System.out.println(answer);
      return parseToJson("success", answer);
    }
  }

  /**
   * This method takes String as an input as returns a JSON object in the required format
   *
   * @param message
   * @return
   */
  public static JsonNode parseToJson(String status, String message) {

    BotResponse botResponse = new BotResponse();
    botResponse.status = status;
    botResponse.message = message;
    System.out.println("BotResponse: " + botResponse.message);
    JsonNode answer = Json.toJson(botResponse);

    return answer;

  }

  public JsonNode displayQuestions(){
    Database database = Databases.inMemory();
    QuestionsDB questionsDb = new QuestionsDB(database);

    String message = questionsDb.displayQuestions();
    return parseToJson("success", message);
  }
}