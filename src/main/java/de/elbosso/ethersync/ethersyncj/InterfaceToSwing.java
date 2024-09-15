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
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;

public class InterfaceToSwing extends Object implements javax.swing.event.DocumentListener

{
    private final static org.slf4j.Logger CLASS_LOGGER =org.slf4j.LoggerFactory.getLogger(InterfaceToSwing.class);
    private final static org.slf4j.Logger EXCEPTION_LOGGER =org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private final JTextArea ta;
    private final EtherSync etherSync;
    private final EtherSyncState etherSyncState;
    private final String fileName;

    public InterfaceToSwing(JTextArea ta, EtherSync etherSync, EtherSyncState etherSyncState, String fileName)
    {
        super();
        this.ta = ta;
        this.etherSync = etherSync;
        this.etherSyncState = etherSyncState;
        this.fileName = fileName;
        ta.getDocument().addDocumentListener(this);
    }

    public void processEvent(org.json.JSONObject event)
    {
        ta.getDocument().removeDocumentListener(this);
        try
        {
            if ("edit".equals(event.getString("method")))
            {
                JSONObject parameters = event.getJSONObject("params");
                JSONObject delta = parameters.getJSONObject("delta");
                int revision = delta.getInt("revision");
                CLASS_LOGGER.debug(revision+" "+etherSyncState.getDaemonRevision());
                if (revision == etherSyncState.getDaemonRevision())
                {
                    org.json.JSONArray deltaa = delta.getJSONArray("delta");
                    for (int i = 0; i < deltaa.length(); ++i)
                    {
                        JSONObject deltad = deltaa.getJSONObject(i);
                        String replacement=deltad.getString("replacement");
                        JSONObject range = deltad.getJSONObject("range");
                        JSONObject start = range.getJSONObject("start");
                        JSONObject end = range.getJSONObject("end");
                        int sline=start.getInt("line");
                        int scharacter=start.getInt("character");
                        int eline=end.getInt("line");
                        int echaracter=end.getInt("character");
                        int soffs=ta.getLineStartOffset(sline)+scharacter;
                        int eoffs=ta.getLineStartOffset(eline)+echaracter;

                        ta.getDocument().remove(soffs,eoffs-soffs);
                        ta.getDocument().insertString(soffs,replacement,null);
                    }
                    etherSyncState.setDaemonRevision(revision);
                }
            }
        }
        catch (BadLocationException | org.json.JSONException e)
        {
            EXCEPTION_LOGGER.warn(e.getMessage(),e);
        }
        ta.getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        CLASS_LOGGER.debug("insertUpdate "+e);
        try
        {
            int offset = e.getOffset();
            int sline = ta.getLineOfOffset(offset);
            int lineStartOffset = ta.getLineStartOffset(sline);
            int scharachter = offset - lineStartOffset;
            offset=e.getOffset()+e.getLength();
            int eline = ta.getLineOfOffset(offset);
            lineStartOffset = ta.getLineStartOffset(eline);
            int echarachter = offset - lineStartOffset;
            org.json.JSONObject edit=EtherSync.createEditMessage(fileName,etherSyncState.getDaemonRevision(),e.getDocument().getText(e.getOffset(),e.getLength()),sline,scharachter,sline,scharachter);
            etherSync.send(edit);
            etherSyncState.incrementEditorRevision();
        }
        catch(BadLocationException | JSONException | InterruptedException exp)
        {
            CLASS_LOGGER.warn(exp.getMessage(),exp);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        try
        {
            CLASS_LOGGER.debug("removeUpdate "+e.getOffset()+" "+e.getLength());
            int offset = e.getOffset();
            int sline = ta.getLineOfOffset(offset);
            int lineStartOffset = ta.getLineStartOffset(sline);
            int scharachter = offset - lineStartOffset;
            offset=e.getOffset()+e.getLength();
            int eline = ta.getLineOfOffset(offset);
            lineStartOffset = ta.getLineStartOffset(eline);
            int echarachter = offset - lineStartOffset;
            org.json.JSONObject edit=EtherSync.createEditMessage(fileName,etherSyncState.getDaemonRevision(),"",sline,scharachter,eline,echarachter);
            etherSync.send(edit);
            etherSyncState.incrementEditorRevision();
        }
        catch(BadLocationException | JSONException | InterruptedException exp)
        {
            CLASS_LOGGER.warn(exp.getMessage(),exp);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        CLASS_LOGGER.debug("changedUpdate "+e);
        //this is ignored here because we are only interested in text changes!!
    }
}
