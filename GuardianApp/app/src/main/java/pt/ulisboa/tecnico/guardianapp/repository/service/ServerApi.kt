package pt.ulisboa.tecnico.guardianapp.repository.service

import android.util.Log
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.grpc.okhttp.OkHttpChannelBuilder
import pt.ulisboa.tecnico.sirs.grpc.LocationRequest
import pt.ulisboa.tecnico.sirs.grpc.ServerServiceGrpc
import java.lang.Exception
import javax.net.ssl.SSLSocketFactory

object ServerApi {

    private const val HOST = "35.181.154.3"
    private const val PORT = 8443

    private val channel: ManagedChannel

    init {
        channel = OkHttpChannelBuilder
            .forAddress(HOST, PORT)
            .hostnameVerifier { hostname, _ ->
                hostname == HOST
            }
            .sslSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
            .build()
    }

    fun getLocation(guardianId: String, childId: String): String {
        val stub = ServerServiceGrpc.newBlockingStub(channel)
        val locationResponse = stub.getLocation(
            LocationRequest
                .newBuilder()
                .setGuardianId(guardianId)
                .setChildId(childId)
                .build())
        return locationResponse.location
    }
}