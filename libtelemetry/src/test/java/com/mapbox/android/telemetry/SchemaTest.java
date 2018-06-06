package com.mapbox.android.telemetry;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SchemaTest {

  @Before
  public void downloadSchema() throws Exception {

    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> bodyRef = new AtomicReference<>();
    final AtomicBoolean failureRef = new AtomicBoolean();
    Callback callback = provideACallback(latch, bodyRef, failureRef);

    Request request = new Request.Builder()
      .url("https://mapbox.s3.amazonaws.com/mapbox-gl-native/event-schema/mobile-event-schemas.jsonl.gz")
      .build();

    OkHttpClient client = new OkHttpClient();
    client.newCall(request).enqueue(callback);

    latch.await();
  }

  @Test
  public void checkArriveSerializing() throws Exception {

  }

  private Callback provideACallback(final CountDownLatch latch, final AtomicReference<String> bodyRef,
                                    final AtomicBoolean failureRef) {
    Callback aCallback = new Callback() {
      @Override
      public void onFailure(Call call, IOException exception) {
        System.out.println("fail: " + exception.getMessage());
        failureRef.set(true);
        latch.countDown();
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        try {
          unpackSchemas(response);
        } catch (IOException exception) {
          throw exception;
        } catch (JSONException exception) {
          exception.printStackTrace();
        } finally {
          latch.countDown();
        }
      }
    };
    return aCallback;
  }

  private void unpackSchemas(Response responseData) throws IOException, JSONException {
    ByteArrayInputStream bais = new ByteArrayInputStream(responseData.body().bytes());
    GZIPInputStream gzis = new GZIPInputStream(bais);
    InputStreamReader reader = new InputStreamReader(gzis);
    BufferedReader in = new BufferedReader(reader);

    String readed;
    while ((readed = in.readLine()) != null) {
      JSONObject jsonObject = new JSONObject(readed);
      System.out.println("string: " + readed);
      System.out.println("JsonObject: " + jsonObject.toString());
    }
  }
}