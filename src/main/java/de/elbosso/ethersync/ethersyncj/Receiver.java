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

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Receiver extends de.elbosso.util.threads.StoppableImpl
{
    private final static org.slf4j.Logger CLASS_LOGGER = org.slf4j.LoggerFactory.getLogger(Receiver.class);
    private final static org.slf4j.Logger EXCEPTION_LOGGER = org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private final de.netsysit.util.threads.CubbyHole<org.json.JSONObject> resultch;
    private final de.netsysit.util.threads.CubbyHole<org.json.JSONObject> messagech;
    private final SocketChannel channel;

    public Receiver(CubbyHole<JSONObject> resultch, CubbyHole<JSONObject> messagech, SocketChannel channel) throws IOException
    {
        super();
        this.resultch = resultch;
        this.messagech = messagech;
        this.channel = channel;
    }


    @Override
    public void run()
    {
        while (!isStopped())
        {
            try
            {
                org.json.JSONObject jsonObject = readMessage(channel);
                if(jsonObject!=null)
                {
                    if (jsonObject.has("result"))
                    {
                        CLASS_LOGGER.debug("passing on result");
                        resultch.put(jsonObject);
                    }
                    else
                    {
                        CLASS_LOGGER.debug("passing on event");
                        messagech.put(jsonObject);
                    }
                }
            }
            catch(java.nio.channels.AsynchronousCloseException e)
            {
                EXCEPTION_LOGGER.warn(e.getMessage(),e);
                stop();
            }
            catch (IOException|org.json.JSONException e)
            {
                EXCEPTION_LOGGER.warn(e.getMessage(), e);
            }
        }
    }

    private static org.json.JSONObject readMessage(SocketChannel channel) throws IOException, JSONException
    {
        org.json.JSONObject rv=null;
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        if (bytesRead >-1)
        {
            byte[] bytes = new byte[bytesRead];
            buffer.flip();
            buffer.get(bytes);
            CLASS_LOGGER.debug("\n"+de.elbosso.util.Utilities.hexdump(bytes));
            rv = new org.json.JSONObject(new String(bytes));
            CLASS_LOGGER.debug("received: "+rv);
        }
        return rv;
    }
}