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

import org.json.JSONException;

import javax.swing.text.BadLocationException;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class EtherSyncApp extends javax.swing.JFrame implements java.awt.event.WindowListener
{
    private final static org.slf4j.Logger CLASS_LOGGER =org.slf4j.LoggerFactory.getLogger(EtherSyncApp.class);
    private final static org.slf4j.Logger EXCEPTION_LOGGER =org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");

    private EtherSync etherSync;
    private de.netsysit.util.threads.CubbyHole<org.json.JSONObject> messagech;
    private EditCallback editCallback;
    private Thread editCallbackThread;
    private static final String fileName="/home/elbosso/src/language_rust/ethersync/daemon/playground/file";
    private final EtherSyncState etherSyncState;
    public static void main(String[]args) throws JSONException, IOException, InterruptedException, BadLocationException
    {
        de.elbosso.util.Utilities.configureBasicStdoutLogging(org.slf4j.event.Level.DEBUG);
        new EtherSyncApp();
    }
    EtherSyncApp() throws JSONException, IOException, InterruptedException, BadLocationException
    {
        javax.swing.JTextArea ta=new javax.swing.JTextArea();
        java.io.FileInputStream fis=new java.io.FileInputStream(fileName);
        ta.getDocument().insertString(0,de.elbosso.util.io.Utilities.readIntoString(fis),null);
        fis.close();
        javax.swing.JScrollPane scroller=new javax.swing.JScrollPane(ta);
        scroller.setPreferredSize(new java.awt.Dimension(800,600));
        etherSync=new EtherSync(new java.io.File("/tmp/ethersync"));
        etherSyncState=new EtherSyncState();
        InterfaceToSwing interfaceToSwing=new InterfaceToSwing(ta,etherSync, etherSyncState, fileName);
        editCallback=new EditCallback(interfaceToSwing,etherSync.getMessagech());
        editCallbackThread=new Thread(editCallback);
        editCallbackThread.start();
        CLASS_LOGGER.debug("sending open message");
        org.json.JSONObject open= EtherSync.createOpenMessage(fileName);
        org.json.JSONObject result=etherSync.send(open);
        CLASS_LOGGER.debug("\n"+(result.toString(2)));
        setContentPane(scroller);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        pack();
        setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e)
    {

    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        try
        {
            org.json.JSONObject close= EtherSync.createCloseMessage(fileName);
            org.json.JSONObject result=etherSync.send(close);
            CLASS_LOGGER.debug("\n"+(result.toString(2)));
            etherSync.close();
            editCallbackThread.interrupt();
        } catch (IOException | JSONException | InterruptedException ex)
        {
            CLASS_LOGGER.warn(ex.getMessage(),ex);
        }
        dispose();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e)
    {

    }

    @Override
    public void windowIconified(WindowEvent e)
    {

    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {

    }

    @Override
    public void windowActivated(WindowEvent e)
    {

    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {

    }
}
