//package models;
//
//import com.mongodb.DB;
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientURI;
//
//import java.net.UnknownHostException;
//
///**
// * Created by chandler on 2/23/17.
// */
//public class DataInterface {
//    private static DataInterface instance;
//    DB zastreamDB;
//
//    public static DataInterface getInstance(){
//        if(instance == null){
//            return new DataInterface();
//        }else{
//            return instance;
//        }
//    }
//
//    public MongoClient getDBClient(){
//        if(mongoClient == null){
//            System.err.println("MONGO CLIENT IS NULL!");
//        }
//        return mongoClient;
//    }
//
//    public DB getZastreamDB(){
//        if(zastreamDB == null){
//            zastreamDB = mongoClient.getDB("ZAStream");
//        }
//        return zastreamDB;
//    }
//
//    public void closeMongoConnection(){
//        if(mongoClient != null){
//            mongoClient.close();
//            mongoClient = null;
//        }
//    }
//
//    private DataInterface(){
//        try {
//            mongoClient  = new MongoClient(new MongoClientURI("mongodb://"+Constants.MONGO_USER+":"+Constants.MONGO_PASS+"@"+Constants.MONGO_IP+":"+Constants.MONGO_PORT));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        instance = this;
//    }
//
//}
