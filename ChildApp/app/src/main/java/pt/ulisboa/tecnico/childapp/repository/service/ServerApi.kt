package pt.ulisboa.tecnico.childapp.repository.service

import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import pt.ulisboa.tecnico.sirs.grpc.AddLocationRequest
import pt.ulisboa.tecnico.sirs.grpc.RegisterRequest
import pt.ulisboa.tecnico.sirs.grpc.ServerServiceGrpc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLSocketFactory


object ServerApi {

    private const val NUMBER_OF_THREADS = 4
    private const val HOST = "35.181.154.3"
    private const val PORT = 8443

    private val channel: ManagedChannel
    val executor: ExecutorService

    init {
        executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
        channel = OkHttpChannelBuilder
            .forAddress(
                HOST,
                PORT
            )
            .hostnameVerifier { hostname, _ ->
                hostname == HOST
            }
            .sslSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
            .build()
    }

    fun registerGuardian(childId: String, guardianId: String): Boolean {
        val stub = ServerServiceGrpc.newBlockingStub(channel)
        val statusResponse = stub.registerGuardian(
            RegisterRequest.newBuilder().setChildId(childId).setGuardianId(guardianId).build())
        return statusResponse.success
    }

    fun addLocation(childId: String, location: String): Boolean {
        val stub = ServerServiceGrpc.newBlockingStub(channel)
        val statusResponse = stub.addLocation(
            AddLocationRequest.newBuilder().setChildId(childId).setLocation(location).build())
        return statusResponse.success
    }

}
