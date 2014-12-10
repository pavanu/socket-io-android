package <package>;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Global Application Class
 */
public class ApplicationName extends Application {

    private final String LOG_TAG = ApplicationName.class.getSimpleName();
    private Socket socket;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        // init
        getSocket();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(LOG_TAG, "onTerminate");
    }

    public synchronized Socket getSocket() {
        if (socket == null) {
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                IO.setDefaultSSLContext(sc);
                HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

                // socket options
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.reconnection = false;
                opts.secure = true;
                opts.sslContext = sc;

                socket = IO.socket("https://(hostname|ip)", opts);
                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(LOG_TAG, "Socket Connected");
                    }

                }).on("message", new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(LOG_TAG, "'message' event: " + args[0].toString());
                    }

                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(LOG_TAG, "Socket Disconnected");
                    }

                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(LOG_TAG, "error: " + args[0].toString());
                    }

                });
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }

        // Connect if disconnected
        if (!socket.connected()) {
            Log.d(LOG_TAG, "Connecting with Socket...");
            socket.connect();
        } else {
            Log.d(LOG_TAG, "Socket Connected");
        }

        return socket;
    }

    private TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    } };

    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}
