/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *
 * @author Administrator
 */
public class MIDIConverter {

   public static Map<String,String> getMMLFromMidi(File midiFile, String mmlName)
   {
        try {
            if(midiFile.isFile())
            {
                Sequence midiSeq = MidiSystem.getSequence(midiFile);
                Track[] tracks = midiSeq.getTracks();
                HashMap<String,String> songTracks = new HashMap<String,String>();
                for(int i = 0; i < tracks.length; i ++)
                {
                    Track thisTrack = tracks[i];
                    Map<Long,Integer> notes = new HashMap<Long,Integer>();
                    for (int j = 0; j < thisTrack.size(); j++) {
                        MidiEvent status = thisTrack.get(j);
                        MidiMessage message = status.getMessage();
                        if (message instanceof ShortMessage) {
                            ShortMessage smessage = (ShortMessage)message;
                            int cmd = smessage.getCommand();
                            if(cmd == 144)
                            {
                                notes.put(status.getTick(), smessage.getData1());
                            }
                        }
                    }
                    if(!notes.isEmpty())
                    {
                        int resolution = midiSeq.getResolution();
                        long totalTicks = midiSeq.getTickLength();
                        long totalms = (long)(midiSeq.getMicrosecondLength() / 1000);
                        int mspernote = (int) (totalms / (totalTicks/resolution));
                        if(mspernote > 999)
                            mspernote = 999;
                        if(mspernote < 100)
                            mspernote = 100;
                        StringBuilder mmlString = new StringBuilder();
                        mmlString.append("[MML]").append("T").append(mspernote).append("O1");
                        int restcount = 0;
                        int lastoct = 0;
                        for(int j = 0; j < totalTicks; j++)
                        {
                            long time = (long)j;
                            if(notes.containsKey(time))
                            {
                                int note = notes.get(time);
                                if(lastoct == 0 && note > 60)
                                {
                                    lastoct = 1;
                                    mmlString.append("o2");
                                }
                                else if(lastoct == 1 && note < 60)
                                {
                                     lastoct = 0;
                                     mmlString.append("o1");
                                }
                                mmlString.append(convertMidiNoteToMMLNote(note));
                                restcount = 0;
                            }
                            else
                            {
                                if(restcount >= resolution)
                                {

                                    mmlString.append("R");
                                    restcount = 0;
                                }
                                else
                                    restcount++;
                            }
                        }
                        songTracks.put(mmlName + "_Track" + i, mmlString.toString());
                    }
                }
                return songTracks;
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(MMLManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
   }

   private static String convertMidiNoteToMMLNote(int in)
   {
        in = (in + 6) % 12;
        if (in == 0) {
            return "F#";
        } else if (in == 1) {
            return "G";
        } else if (in == 2) {
            return "G#";
        } else if (in == 3) {
            return "A";
        } else if (in == 4) {
            return "A#";
        } else if (in == 5) {
            return "B";
        } else if (in == 6) {
            return "C";
        } else if (in == 7) {
            return "C#";
        } else if (in == 8) {
            return "D";
        } else if (in == 9) {
            return "D#";
        } else if (in == 10) {
            return "E";
        } else if (in == 11) {
            return "F";
        }
        return "";
    }
}
