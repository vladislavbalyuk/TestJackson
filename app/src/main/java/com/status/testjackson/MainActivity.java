package com.status.testjackson;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGen, btnRead, btnGenTree, btnReadTree, btnGenStream, btnReadStream;
    String jsonString;
    ObjectMapper mapper;
    OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGen = (Button) findViewById(R.id.btnGen);
        btnGen.setOnClickListener(this);
        btnRead = (Button) findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);
        btnGenTree = (Button) findViewById(R.id.btnGenTree);
        btnGenTree.setOnClickListener(this);
        btnReadTree = (Button) findViewById(R.id.btnReadTree);
        btnReadTree.setOnClickListener(this);
        btnGenStream = (Button) findViewById(R.id.btnGenStream);
        btnGenStream.setOnClickListener(this);
        btnReadStream = (Button) findViewById(R.id.btnReadStream);
        btnReadStream.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Job job;
        Human human;
        List<Human> list;
        int id = v.getId();
        switch (id) {
            case R.id.btnGen:

                list = getData();

                mapper = new ObjectMapper();
                jsonString = null;
                try {
                    jsonString = mapper.writeValueAsString(list);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                Log.d("MyLog", jsonString);
                break;
            case R.id.btnRead:
                mapper = new ObjectMapper();

                try {
                    list = mapper.readValue(jsonString, new TypeReference<ArrayList<Human>>() {
                    });
                    for (Human h : list) {
                        h.info();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnGenTree:

                outputStream = new ByteArrayOutputStream();

                list = getData();
                jsonString = null;

                mapper = new ObjectMapper();
                ArrayNode arrayNode = mapper.createArrayNode();
                for (Human h : list) {
                    ObjectNode rootNode = arrayNode.addObject();
                    rootNode.put("age", h.getAge());
                    rootNode.put("name", h.getName());
                    ObjectNode childNode = rootNode.putObject("job");
                    childNode.put("salary", h.getJob().getSalary());
                    childNode.put("name", h.getJob().getName());

                }
                try {
                    mapper.writeValue(outputStream, arrayNode);
                } catch (Exception e) {
                }
                ;

                jsonString = outputStream.toString();
                Log.d("MyLog", jsonString);
                break;
            case R.id.btnReadTree:
                JsonNode rootNode = null;
                ObjectMapper mapper = new ObjectMapper();
                try {
                    rootNode = mapper.readValue(jsonString, JsonNode.class);
                } catch (Exception e) {
                }
                ;
                for (int i = 0; i < rootNode.size(); i++) {
                    JsonNode humanNode = rootNode.get(i);
                    JsonNode jobNode = humanNode.get("job");

                    job = new Job();
                    job.setName(jobNode.get("name").asText());
                    job.setSalary(jobNode.get("salary").asInt());

                    human = new Human();
                    human.setName(humanNode.get("name").asText());
                    human.setAge(humanNode.get("age").asInt());
                    human.setJob(job);
                    human.info();
                }
                break;
            case R.id.btnGenStream:
                JsonWriter writer = null;
                outputStream = new ByteArrayOutputStream();

                list = getData();
                jsonString = null;

                try {
                    writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.beginArray();
                    for(Human h: list){
                        writer.beginObject();
                        writer.name("age");
                        writer.value(h.getAge());
                        writer.name("name");
                        writer.value(h.getName());
                        job = h.getJob();
                        writer.name("job");
                        writer.beginObject();
                        writer.name("name");
                        writer.value(job.getName());
                        writer.name("salary");
                        writer.value(job.getSalary());
                        writer.endObject();
                        writer.endObject();
                    }
                    writer.endArray();
                    writer.close();
                }
                catch (Exception e){};
                jsonString = outputStream.toString();
                Log.d("MyLog", jsonString);
                break;
            case R.id.btnReadStream:
                JsonParser jsonParser = null;
                JsonToken jsonToken = null;
                JsonFactory jsonFactory = new JsonFactory();
                try {
                    jsonParser = jsonFactory.createParser(jsonString);
                    jsonToken = jsonParser.nextToken();
                    human = null;
                    job = null;
                    boolean isJob = false;
                    boolean isFieldAge = false, isFieldName = false, isFieldSalary = false;
                    while(jsonParser.hasCurrentToken()) {
                        if(jsonToken == JsonToken.START_OBJECT && human == null) {
                            human = new Human();
                        }

                        if(jsonToken == JsonToken.START_OBJECT && human != null) {
                            isJob = true;
                            job = new Job();
                        }

                        if(jsonToken == JsonToken.END_OBJECT) {
                            if(!isJob){
                                human.info();
                                human = null;
                            }
                            else {
                                human.setJob(job);
                                isJob = false;
                            }
                        }

                        if(jsonToken == JsonToken.FIELD_NAME){
                            if(jsonParser.getText().equals("age")){
                                isFieldAge = true;
                            }
                            else if(jsonParser.getText().equals("name")){
                                isFieldName = true;
                            }
                            else if(jsonParser.getText().equals("salary")){
                                isFieldSalary = true;
                            }
                        }

                        if(jsonToken == JsonToken.VALUE_STRING) {
                            if(isFieldName){
                                if(isJob){
                                    job.setName(jsonParser.getText());
                                }
                                else {
                                    human.setName(jsonParser.getText());
                                }
                                isFieldName = false;
                            }
                        }

                        if(jsonToken == JsonToken.VALUE_NUMBER_INT) {
                            if(isFieldAge){
                                human.setAge(jsonParser.getIntValue());
                                isFieldAge = false;
                            }
                            if(isFieldSalary){
                                job.setSalary(jsonParser.getIntValue());
                                isFieldSalary = false;
                            }
                        }

                        jsonToken = jsonParser.nextToken();
                    }
                }
                catch (Exception e){};
                break;
        }


    }

    private List<Human> getData() {
        Job job1 = new Job();
        job1.setSalary(10000);
        job1.setName("driver");
        Job job2 = new Job();
        job2.setSalary(30000);
        job2.setName("programmist");

        Human human1 = new Human();
        human1.setAge(45);
        human1.setName("Victor");
        human1.setJob(job1);
        Human human2 = new Human();
        human2.setAge(35);
        human2.setName("Sergey");
        human2.setJob(job2);

        List<Human> list = new ArrayList<Human>();
        list.add(human1);
        list.add(human2);

        return list;
    }

}
