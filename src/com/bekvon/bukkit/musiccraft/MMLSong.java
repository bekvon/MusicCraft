/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 *
 * @author Gary Smoak
 */
public class MMLSong {
    private String origMML = "";
    private byte[] notes = new byte[0];
    private int[] tempo = new int[0];
    private boolean repeat=false;
    private String songComposer;
    private String songName;

    public static MMLSong parseMML(String linesIn) {
        return parseMML(linesIn,null,null);
    }

    public static MMLSong parseMML(String linesIn, String composer, String name)
    {
        if(linesIn == null)
            return null;
        MMLSong thisSong = new MMLSong();
        thisSong.origMML = linesIn;
        thisSong.songComposer = composer;
        thisSong.songName = name;
        ByteArrayOutputStream outNotes = new ByteArrayOutputStream();
        ArrayList<Integer> outTempo = new ArrayList<Integer>();
        int modifier = 0;
        int lastTempo = 500;
        char[] notes = linesIn.toCharArray();
        int rNoteNum = (MusicCraft.getManager().getMaxOctet() * 12) + 1;
        for(int i = 0; i < notes.length; i ++)
        {
            char thisnote = notes[i];
            int thisnumericnote = -1;
            if(i < notes.length - 1 && (thisnote == 't' || thisnote == 'T'))
            {
                try {
                    char n = notes[i + 1];
                    lastTempo = Integer.parseInt(Character.toString(n)) * 100;
                } catch (Exception ex) {
                }
                try {
                    char n = notes[i + 1];
                    char n2 = notes[i + 2];
                    char n3 = notes[i + 3];
                    lastTempo = Integer.parseInt(Character.toString(n) + Character.toString(n2) + Character.toString(n3));
                } catch (Exception ex) {
                }
                if(lastTempo<100)
                    lastTempo=100;
                i = i + 1;
            } else if (i < notes.length - 1 && (thisnote == 'o' || thisnote == 'O')) {
                try {
                    char octave = notes[i + 1];
                    int oct = Integer.parseInt(Character.toString(octave));
                    if(oct>MusicCraft.getManager().getMaxOctet())
                    {
                        oct = MusicCraft.getManager().getMaxOctet();
                    }
                    modifier = (oct - 1) * 12;
                } catch (Exception ex) {
                }
                i = i + 1;
            }
            else if(thisnote == '>')
            {
                modifier = modifier + 12;
            }
            else if(thisnote == '<')
            {
                modifier = modifier - 12;
                if(modifier < 0)
                {
                    modifier = 0;
                }
            }
            else if (thisnote == 'g' || thisnote == 'G') {
                thisnumericnote = 1 + modifier;
            } else if (thisnote == 'a' || thisnote == 'A') {
                thisnumericnote = 3 + modifier;
            } else if (thisnote == 'b' || thisnote == 'B') {
                thisnumericnote = 5 + modifier;
            } else if (thisnote == 'c' || thisnote == 'C') {
                thisnumericnote = 6 + modifier;
            } else if (thisnote == 'd' || thisnote == 'D') {
                thisnumericnote = 8 + modifier;
            } else if (thisnote == 'e' || thisnote == 'E') {
                thisnumericnote = 10 + modifier;
            } else if (thisnote == 'f' || thisnote == 'F') {
                thisnumericnote = 11 + modifier;
            }
            else if(thisnote == 'r' || thisnote == 'R')
            {
                thisnumericnote = rNoteNum;
            }
            else if(thisnote == 'x' || thisnote == 'X')
            {
                thisSong.repeat = true;
            }
            if(i < notes.length - 1 && (notes[i+1] == '#' || notes[i+1] == '+'))
            {
                if(notes[i] == 'f' || notes[i] == 'F')
                    thisnumericnote = thisnumericnote - 11;
                else
                    thisnumericnote = thisnumericnote + 1;
            }
            if(thisnumericnote <= rNoteNum && thisnumericnote >=0)
            {
                outNotes.write(thisnumericnote);
                outTempo.add(lastTempo);
            }
        }
        thisSong.notes = outNotes.toByteArray();
        thisSong.tempo = new int[thisSong.notes.length];
        for(int i = 0 ; i < thisSong.notes.length; i++)
        {
            thisSong.tempo[i] = outTempo.get(i);
        }
        return thisSong;
    }

    public int getNoteCount()
    {
        return notes.length;
    }

    public byte getNote(int index)
    {
        if(index >= 0 && index < notes.length)
            return notes[index];
        else
            return -1;
    }

    public byte[] getNotes()
    {
        return notes;
    }
    
    public int[] getNoteTempos()
    {
        return tempo;
    }

    public void setMinTempo(int t)
    {
        for(int i = 0; i < tempo.length; i ++)
        {
            if(tempo[i]<t)
            {
                tempo[i] = t;
            }
        }
    }

    public boolean isRepeat()
    {
        return repeat;
    }

    public void setRepeat(boolean r)
    {
        repeat = r;
    }

    public String getOrigScript()
    {
        return origMML;
    }

    public String getComposer()
    {
        if(songComposer == null)
            return "";
        else
            return songComposer;
    }

    public String getSongName()
    {
        if(songName == null)
            return "";
        else
            return songName;
    }

}
