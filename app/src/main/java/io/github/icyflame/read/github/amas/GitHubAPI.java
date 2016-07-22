package io.github.icyflame.read.github.amas;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by siddharth on 19/6/16.
 */
public interface GitHubAPI {
    @GET("repos/{user}/{repo}/issues?state=closed")
    Call<List<JsonObject>> listIssues(@Path("user") String user, @Path("repo") String repo);

    @GET("repos/{user}/{repo}/issues/{number}/comments")
    Call<List<JsonObject>> listIssueComments(@Path("user") String user, @Path("repo") String repo, @Path("number") String number);
}
