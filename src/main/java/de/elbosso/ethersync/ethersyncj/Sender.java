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
import org.json.JSONObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Sender extends de.elbosso.util.threads.StoppableImpl
{
    private final static org.slf4j.Logger CLASS_LOGGER =org.slf4j.LoggerFactory.getLogger(Sender.class);
    private final static org.slf4j.Logger EXCEPTION_LOGGER =org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private final de.netsysit.util.threads.CubbyHole<org.json.JSONObject> ch;
    private final SocketChannel channel;

    public Sender(CubbyHole<org.json.JSONObject> ch, SocketChannel channel) throws IOException
    {
        super();
        this.ch = ch;
        this.channel = channel;
    }


    @Override
    public void run()
    {
        while(!isStopped())
        {
            try
            {
                org.json.JSONObject jsonObject=ch.get();
                sendMessage(jsonObject,channel);

            }
            catch(java.nio.channels.AsynchronousCloseException e)
            {
                EXCEPTION_LOGGER.warn(e.getMessage(),e);
                stop();
            }
            catch (InterruptedException|IOException e)
            {
                EXCEPTION_LOGGER.warn(e.getMessage(),e);
            }
        }
    }

    private void sendMessage(org.json.JSONObject msg, SocketChannel channel) throws IOException
    {
        String message=produceMessage(msg);
        message=massage(message);
        byte[] bytes=message.getBytes();
        CLASS_LOGGER.debug("\n"+de.elbosso.util.Utilities.hexdump(bytes));
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(bytes.length);
        buffer.clear();
        buffer.put(bytes);
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
    /**
     * Messages have to end on \r\n - so we make sure they do!
     * @param message
     * @return
     */
    private String massage(String message)
    {
        String rv=message;
        if(message.endsWith("\r\n")==false)
        {
            if (message.endsWith("\n"))
                rv=(message.substring(0,message.length()-1)+"\r\n");
            else
                rv=(message + "\r\n");
        }
        CLASS_LOGGER.debug("final message is: "+rv);
        return rv;
    }

    private String produceMessage(JSONObject msg)
    {
        return msg.toString();
    }
}
