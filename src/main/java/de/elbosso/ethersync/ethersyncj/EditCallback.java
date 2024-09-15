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

public class EditCallback extends de.elbosso.util.threads.StoppableImpl
{
    private final static org.slf4j.Logger CLASS_LOGGER =org.slf4j.LoggerFactory.getLogger(EditCallback.class);
    private final static org.slf4j.Logger EXCEPTION_LOGGER =org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private final de.netsysit.util.threads.CubbyHole<org.json.JSONObject> ch;
    private final InterfaceToSwing interfaceToSwing;

    public EditCallback(InterfaceToSwing interfaceToSwing,CubbyHole<JSONObject> ch) throws IOException
    {
        super();
        this.interfaceToSwing=interfaceToSwing;
        this.ch = ch;
    }

    @Override
    public void run()
    {
        CLASS_LOGGER.debug("started EditCallback");
        while(!isStopped())
        {
            try
            {
                org.json.JSONObject jsonObject=ch.get();
                CLASS_LOGGER.debug("received: "+(jsonObject.toString(2)));
                interfaceToSwing.processEvent(jsonObject);
            }
            catch (InterruptedException|org.json.JSONException e)
            {
                EXCEPTION_LOGGER.warn(e.getMessage(),e);
            }
        }
    }
}
