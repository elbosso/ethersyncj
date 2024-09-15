/*
 * Copyright (c) 2024.
 *
 * Juergen Key. Alle Rechte vorbehalten.
 *
 * Weiterverbreitung und Verwendung in nichtkompilierter oder kompilierter Form,
 * mit oder ohne Veraenderung, sind unter den folgenden Bedingungen zulaessig:
 *
 *    1. Weiterverbreitete nichtkompilierte Exemplare muessen das obige Copyright,
 * die Liste der Bedingungen und den folgenden Haftungsausschluss im Quelltext
 * enthalten.
 *    2. Weiterverbreitete kompilierte Exemplare muessen das obige Copyright,
 * die Liste der Bedingungen und den folgenden Haftungsausschluss in der
 * Dokumentation und/oder anderen Materialien, die mit dem Exemplar verbreitet
 * werden, enthalten.
 *    3. Weder der Name des Autors noch die Namen der Beitragsleistenden
 * duerfen zum Kennzeichnen oder Bewerben von Produkten, die von dieser Software
 * abgeleitet wurden, ohne spezielle vorherige schriftliche Genehmigung verwendet
 * werden.
 *
 * DIESE SOFTWARE WIRD VOM AUTOR UND DEN BEITRAGSLEISTENDEN OHNE
 * JEGLICHE SPEZIELLE ODER IMPLIZIERTE GARANTIEN ZUR VERFUEGUNG GESTELLT, DIE
 * UNTER ANDEREM EINSCHLIESSEN: DIE IMPLIZIERTE GARANTIE DER VERWENDBARKEIT DER
 * SOFTWARE FUER EINEN BESTIMMTEN ZWECK. AUF KEINEN FALL IST DER AUTOR
 * ODER DIE BEITRAGSLEISTENDEN FUER IRGENDWELCHE DIREKTEN, INDIREKTEN,
 * ZUFAELLIGEN, SPEZIELLEN, BEISPIELHAFTEN ODER FOLGENDEN SCHAEDEN (UNTER ANDEREM
 * VERSCHAFFEN VON ERSATZGUETERN ODER -DIENSTLEISTUNGEN; EINSCHRAENKUNG DER
 * NUTZUNGSFAEHIGKEIT; VERLUST VON NUTZUNGSFAEHIGKEIT; DATEN; PROFIT ODER
 * GESCHAEFTSUNTERBRECHUNG), WIE AUCH IMMER VERURSACHT UND UNTER WELCHER
 * VERPFLICHTUNG AUCH IMMER, OB IN VERTRAG, STRIKTER VERPFLICHTUNG ODER
 * UNERLAUBTE HANDLUNG (INKLUSIVE FAHRLAESSIGKEIT) VERANTWORTLICH, AUF WELCHEM
 * WEG SIE AUCH IMMER DURCH DIE BENUTZUNG DIESER SOFTWARE ENTSTANDEN SIND, SOGAR,
 * WENN SIE AUF DIE MOEGLICHKEIT EINES SOLCHEN SCHADENS HINGEWIESEN WORDEN SIND.
 *
 */

package de.elbosso.ethersync.ethersyncj;

import de.netsysit.util.threads.CubbyHole;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class EtherSync extends Thread implements Closeable
{
    private final static org.slf4j.Logger CLASS_LOGGER =org.slf4j.LoggerFactory.getLogger(EtherSync.class);
    private final static org.slf4j.Logger EXCEPTION_LOGGER =org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private SocketChannel channel;
    private de.netsysit.util.threads.CubbyHole<org.json.JSONObject> sendch= new de.netsysit.util.threads.SimpleBufferingCubbyHole();
    private de.netsysit.util.threads.CubbyHole<org.json.JSONObject> resultch= new de.netsysit.util.threads.SimpleBufferingCubbyHole();
    private de.netsysit.util.threads.CubbyHole<org.json.JSONObject> messagech= new de.netsysit.util.threads.SimpleBufferingCubbyHole();
    private Sender sender;
    private Receiver receiver;
    private Thread senderThread;
    private Thread receiverThread;

    EtherSync(java.io.File socketFile) throws JSONException, IOException, InterruptedException
    {
        java.nio.file.Path socketFilePath=java.nio.file.Paths.get(socketFile.toURI());
        java.net.UnixDomainSocketAddress unixDomainSocketAddress=java.net.UnixDomainSocketAddress.of(socketFilePath);
        channel = SocketChannel.open(java.net.StandardProtocolFamily.UNIX);
        channel.connect(unixDomainSocketAddress);
        sender=new Sender(sendch,channel);
        receiver=new Receiver(resultch,messagech,channel);
        senderThread=new Thread(sender);
        senderThread.start();
        receiverThread=new Thread(receiver);
        receiverThread.start();
    }

    public CubbyHole<JSONObject> getMessagech()
    {
        return messagech;
    }

    public static JSONObject createOpenMessage(String filename) throws JSONException
    {
        JSONObject rv=new JSONObject();
        rv.put("jsonrpc","2.0");
        rv.put("id",1);//uuidAsBigInteger(java.util.UUID.randomUUID()).abs());
        rv.put("method","open");
        JSONObject parameters=new JSONObject();
        parameters.put("uri",new java.io.File(filename).toPath().toUri().toString());
        rv.put("params",parameters);
        return rv;
    }

    public static JSONObject createCloseMessage(String filename) throws JSONException
    {
        JSONObject rv=new JSONObject();
        rv.put("jsonrpc","2.0");
        rv.put("id",1);//uuidAsBigInteger(java.util.UUID.randomUUID()).abs());
        rv.put("method","close");
        JSONObject parameters=new JSONObject();
        parameters.put("uri",new java.io.File(filename).toPath().toUri().toString());
        rv.put("params",parameters);
        return rv;
    }

    public static JSONObject createEditMessage(String filename, int revision, String replacement, int sline, int scharacter, int eline, int echaracter) throws JSONException
    {
        JSONObject rv=new JSONObject();
        rv.put("jsonrpc","2.0");
        rv.put("id",1);//uuidAsBigInteger(java.util.UUID.randomUUID()).abs());
        rv.put("method","edit");
        JSONObject parameters=new JSONObject();
        parameters.put("uri",new java.io.File(filename).toPath().toUri().toString());
        JSONObject delta=new JSONObject();
        delta.put("revision",revision);
        org.json.JSONArray deltaa=new org.json.JSONArray();
        JSONObject deltad=new JSONObject();
        deltad.put("replacement",replacement);
        JSONObject range=new JSONObject();
        JSONObject start=new JSONObject();
        start.put("line",sline);
        start.put("character",scharacter);
        range.put("start", start);
        JSONObject end=new JSONObject();
        end.put("line",eline);
        end.put("character",echaracter);
        range.put("end", end);
        deltad.put("range", range);
        deltaa.put(deltad);
        delta.put("delta",deltaa);
        parameters.put("delta",delta);
        rv.put("params",parameters);
        return rv;
    }


    public org.json.JSONObject send(org.json.JSONObject msg) throws InterruptedException
    {
        sendch.put(msg);
        return resultch.get();
    }



    public static java.math.BigInteger uuidAsBigInteger(java.util.UUID id)
    {
        long hi = id.getMostSignificantBits();
        long lo = id.getLeastSignificantBits();
        byte[] bytes = java.nio.ByteBuffer.allocate(16).putLong(hi).putLong(lo).array();
        java.math.BigInteger big = new java.math.BigInteger(bytes);
        return big;
    }

    @Override
    public void close() throws IOException
    {
        channel.close();
        sender.stop();
        receiver.stop();
        senderThread.interrupt();
    }
}
