
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.sirs.grpc.*;

import java.sql.SQLException;
import java.util.List;

public class ServerService extends ServerServiceGrpc.ServerServiceImplBase {
    private DBUtils db;

    public ServerService(DBUtils db) {
        this.db = db;
    }

    @Override
    public void registerGuardian(RegisterRequest request, StreamObserver<StatusResponse> responseObserver) {
        System.out.println("$ RegisterGuardian with childId=" + request.getChildId() + " and guardianId=" + request.getGuardianId());
        try {
            db.insertGuardian(request.getChildId(), request.getGuardianId());
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(false).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addLocation(AddLocationRequest request, StreamObserver<StatusResponse> responseObserver) {
        System.out.println("$ AddLocation with childId=" + request.getChildId() + " and location=" + request.getLocation());
        try {
            db.insertLocation(request.getChildId(), request.getLocation());
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onNext(StatusResponse.newBuilder().setSuccess(false).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getLocation(LocationRequest request, StreamObserver<LocationResponse> responseObserver) {
        System.out.println("$ GetLocation with guardianId=" + request.getGuardianId() + " and childId=" + request.getChildId());
        try {
            String location = db.selectLastLocation(request.getGuardianId(), request.getChildId());
            if (location == null) {
                responseObserver.onNext(LocationResponse.newBuilder().setLocation("").build());
            }
            else {
                responseObserver.onNext(LocationResponse.newBuilder().setLocation(location).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onNext(LocationResponse.newBuilder().setLocation("").build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getLocationHistory(LocationRequest request, StreamObserver<LocationResponse> responseObserver) {
        System.out.println("$ GetLocationHistory with guardianId=" + request.getGuardianId() + " and childId=" + request.getChildId());
        try {
            List<String> locations = db.selectAllLocations(request.getGuardianId(), request.getChildId());
            locations.forEach(it ->
                    responseObserver.onNext(LocationResponse.newBuilder().setLocation(it).build())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        responseObserver.onCompleted();
    }

    @Override
    public void updateLocation(LocationRequest request, StreamObserver<LocationResponse> responseObserver) {
        System.out.println("$ GetLocationHistory with guardianId=" + request.getGuardianId() + " and childId=" + request.getChildId());
        try {
            List<String> locations = db.updateLocations(request.getGuardianId(), request.getChildId());
            locations.forEach(it ->
                    responseObserver.onNext(LocationResponse.newBuilder().setLocation(it).build())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        responseObserver.onCompleted();
    }

    @Override
    public void childSOS(SOSMessage request, StreamObserver<StatusResponse> responseObserver) {
        super.childSOS(request, responseObserver);
    }
}
