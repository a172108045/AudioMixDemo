package com.example.m3b.mixtest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.m3b.rbaudiomixlibrary.AudioRecorderNative;
import com.m3b.rbaudiomixlibrary.MusicPlayer;
import com.m3b.rbaudiomixlibrary.Record;
import com.m3b.rbaudiomixlibrary.WaveCanvas;
import com.m3b.rbaudiomixlibrary.view.WaveSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity {
    private static int count;
    private final int _iSampleRateDef = 16000;//44100;//do not modify,16000 is good for meizu phone
    private final String SAVE_FILENAME = "record_file.aac";
    private final String NEW_SAVE_FILENAME = "new_record_file.aac";
    private final String MP3_FILENAME = "test.mp3";
    private final String MP3_FILENAME2 = "test2.mp3";
    private final String MP3_FILENAME3 = "test3.mp3";
    public AudioRecorderNative _AudioMix;
    protected ProgressBar micProgressBar;
    protected ProgressBar musicProgressBar;
    protected WaveCanvas mwaveCanvas;
    private String LOGMODULE = "MainActivity";
    private Button _RecordStopButton;
    private Button _RecordCutButton;
    private AudioRecord _AudioRecorder = null;
    private int _iRecorderBufferSize;
    private byte[] _RecorderBuffer;
    private boolean _mbStop;
    private FileOutputStream aacDataOutStream = null;
    private String[] params = new String[]{Environment.getExternalStorageDirectory().getPath() + "/" + MP3_FILENAME3, Environment.getExternalStorageDirectory().getPath() + "/" + MP3_FILENAME2};
    private byte[] bgm;
    private boolean noMic;
    private Button _RecordPauseButton;
    private boolean noMusic;
    private Button _MusicPauseButton;
    private float fMusicGain = 0.05f;
    private float fMicGain = 1.0f;
    private SeekBar _MusicSeekBar;
    private SeekBar _MicSeekBar;
    private Button _PlayStartButton;
    private MediaPlayer mPlayer;
    private Button _ChangeMusicButton;
    private MusicPlayTask musicPlayTask;
    private int bgmIndex = 0;
    private MusicPlayer mMusicPlayer;
    private boolean isMixBgm = false;
    private MusicIntentReceiver myReceiver;
    private byte[] result;
    private boolean mBackLoop = true;
    private WaveSurfaceView waveSfv;


    private int AudioMixInit() {
        int iRet;
        iRet = _AudioMix.NativeInit();
        Log.i(LOGMODULE, "NativeInit return " + iRet + "....");

        return iRet;
    }


    private void WaveCanvasInit() {
        mwaveCanvas = new WaveCanvas();
        mwaveCanvas.baseLine = waveSfv.getHeight() / 2;
        mwaveCanvas.init(waveSfv);

        waveSfv.setVisibility(View.VISIBLE);
    }


    private void CreatAACFile() {
        String strPath = Environment.getExternalStorageDirectory().getPath();
        String filename = strPath + "/" + SAVE_FILENAME;
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        try {
            aacDataOutStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void SetButtonEvent() {
        _RecordStopButton = (Button) findViewById(R.id.RecordStopButton);
        _RecordCutButton = (Button) findViewById(R.id.RecordCutButton);
        _RecordPauseButton = (Button) findViewById(R.id.RecordPauseButton);
        _MusicPauseButton = (Button) findViewById(R.id.MusicPauseButton);
        _PlayStartButton = (Button) findViewById(R.id.PlayStartButton);
        _ChangeMusicButton = (Button) findViewById(R.id.ChangeMusicButton);

        _MusicSeekBar = ((SeekBar) findViewById(R.id.MusicSeekBar));
        _MicSeekBar = ((SeekBar) findViewById(R.id.MicSeekBar));

        _mbStop = true;
        noMic = false;
        noMusic = false;

        _MusicSeekBar.setProgress((int) (fMusicGain * 100));
        _MicSeekBar.setProgress((int) (fMicGain * 100));

        _RecordStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (_mbStop) {
//                    return;
//                }
//                _mbStop = true;
//                noMusic = false;
//
//                if (_AudioRecorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED)
//                    _AudioRecorder.stop();
//
//                try {
//                    aacDataOutStream.close();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//                //mMusicPlayer.release();
//
//                _RecordPauseButton.setText("开始录制");
//                _MusicPauseButton.setText("关闭音乐");
//
//                noMusic = false;
//                count = 0;
//
//
//                waveSfv.setVisibility(View.INVISIBLE);

                Record.instance().stopRecord();

            }
        });

        _RecordCutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_mbStop) {
                    return;
                }
                String strPath = Environment.getExternalStorageDirectory().getPath();
                String filename = strPath + "/" + SAVE_FILENAME;
                String outputFilename = strPath + "/" + NEW_SAVE_FILENAME;

                File file = new File(outputFilename);
                if (file.exists()) {
                    file.delete();
                }
                _AudioMix.AudioFileCut(filename, outputFilename, 5, 10, 64000);
            }
        });


        _RecordPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                noMic = !noMic;
//                if (++count == 1) {
//
//                CreatAACFile();
//                    AudioMixInit();
//                    WaveCanvasInit();
//
////                    musicPlayTask = new MusicPlayTask();
////                    musicPlayTask.execute(params);
//
//                    noMic = false;
//                    noMusic = true;
//
//                    StartMixThread();
//
//                    _RecordPauseButton.setText("暂停录制");
//                } else {
//                    if (noMic) {
//                        _RecordPauseButton.setText("继续录制");
//                    } else {
//                        _RecordPauseButton.setText("暂停录制");
//                    }
//                }

                String strPath = Environment.getExternalStorageDirectory().getPath();
                String filename = strPath + "/" + SAVE_FILENAME;
                Record.instance().startRecord("", filename, new Record.RecordListener() {
                    @Override
                    public void onRecordStart(String bgmPath, String filePath) {
                        L.e("call: onRecordStart([bgmPath, filePath])-> " + filePath);
                        WaveCanvasInit();
                    }

                    @Override
                    public void onRecordStop(String bgmPath, String filePath) {
                        L.e("call: onRecordStop([bgmPath, filePath])-> " + filePath);

                        try {
                            mPlayer = new MediaPlayer();
                            mPlayer.setDataSource(filePath);
                            mPlayer.prepare();
                            mPlayer.start();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onRecordError(Exception error) {
                        L.e("call: onRecordError([error])-> " + error.getMessage());
                    }

                    @Override
                    public void onAmplitudeChanged(int amplitude) {
                        L.e("call: onAmplitudeChanged([amplitude])-> " + amplitude);
                        musicProgressBar.setProgress(amplitude);
                    }

                    @Override
                    public void onAudioDataChanged(short[] buffer, int readsize, boolean mformRight) {
                        L.e("call: onAudioDataChanged([buffer, readsize, mformRight])-> ");
                        mwaveCanvas.updateAudioData(buffer, readsize, mformRight);
                    }
                });
            }
        });

        _MusicPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noMusic = !noMusic;
                if (noMusic) {
                    _MusicPauseButton.setText("开启音乐");
                    if (mMusicPlayer != null)
                        mMusicPlayer.setNeedPlayPause(true);
                } else {
                    _MusicPauseButton.setText("关闭音乐");
                    if (mMusicPlayer != null)
                        mMusicPlayer.setNeedPlayPause(false);
                }
            }
        });

        _PlayStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/" + SAVE_FILENAME);
                    mPlayer.prepare();
                    mPlayer.start();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });


        _ChangeMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bgmIndex = 1 - bgmIndex;
                if (mMusicPlayer != null) {
                    mMusicPlayer.release();
                    musicPlayTask = new MusicPlayTask();
                    musicPlayTask.execute(params);
                }

            }
        });

        _MusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                fMusicGain = progress / 100f;
                if (mMusicPlayer != null)
                    mMusicPlayer.setPlayVolume(progress / 100f);
                //Log.d("###", " -- " + fMusicGain);
            }
        });


        _MicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                fMicGain = progress / 100f;
                //Log.d("###", " -- " + fMicGain);
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS},
                1000);


        micProgressBar = (ProgressBar) findViewById(R.id.MicVol);
        musicProgressBar = (ProgressBar) findViewById(R.id.MusicVol);

        //mWaveformView = (WaveformView) findViewById(R.id.waveform_view);

        waveSfv = (WaveSurfaceView) findViewById(R.id.wavesfv);
        waveSfv.setLine_off(2);


        myReceiver = new MusicIntentReceiver();

        _AudioMix = new AudioRecorderNative();
        this.SetButtonEvent();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        _mbStop = true;
        noMusic = false;
        if (_AudioRecorder != null)
            _AudioRecorder.stop();

        if (aacDataOutStream != null) {
            try {
                aacDataOutStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            aacDataOutStream = null;
        }
        _RecordPauseButton.setText("开始录制");
        _MusicPauseButton.setText("关闭音乐");
        count = 0;

    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _mbStop = true;
        noMusic = false;
        if (_AudioRecorder != null)
            _AudioRecorder.stop();

        if (aacDataOutStream != null) {
            try {
                aacDataOutStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            aacDataOutStream = null;
        }
        _RecordPauseButton.setText("开始录制");
        _MusicPauseButton.setText("关闭音乐");
        count = 0;
    }


    private void StartMixThread() {
        Thread MixEncodeThread = new Thread(new Runnable() {
            @Override
            public void run() {

                _mbStop = false;

                _iRecorderBufferSize = AudioRecord.getMinBufferSize(_iSampleRateDef,
                        AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT);
                _AudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        _iSampleRateDef, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, _iRecorderBufferSize);


                while (!_mbStop) {

                    _RecorderBuffer = new byte[_iRecorderBufferSize];

                    _AudioRecorder.startRecording();

                    int iMicLen = _AudioRecorder.read(_RecorderBuffer, 0, _RecorderBuffer.length); // Fill buffer

                    if (iMicLen != AudioRecord.ERROR_BAD_VALUE) {

                        _AudioMix.SetMusicVol(fMusicGain);
                        _AudioMix.SetVoiceVol(fMicGain * 1.5f);

                        if (noMic && !noMusic) {

                            bgm = mMusicPlayer.getBackGroundBytes();

                            if (bgm != null) {
                                result = _AudioMix.MusicEncode(44100, 2, bgm, bgm.length);
                                showAmplitude(bgm, bgm.length, musicProgressBar, fMusicGain);
                                drawWavefrom();
                            } else {
                                if (!mMusicPlayer.isPlayingMusic() && mMusicPlayer.isPCMDataEos() && mBackLoop) {
                                    mMusicPlayer.release();
                                    mMusicPlayer.startPlayBackMusic();
                                }
                                continue;
                            }
                        } else if (!noMic && noMusic) {
                            result = _AudioMix.VoiceEncode(_iSampleRateDef, 2, _RecorderBuffer, iMicLen);
                            showAmplitude(_RecorderBuffer, iMicLen, micProgressBar, fMicGain);
                            drawWavefrom();
                        } else if (!noMic && !noMusic) {
                            bgm = mMusicPlayer.getBackGroundBytes();

                            if (bgm != null)
                                showAmplitude(bgm, bgm.length, musicProgressBar, fMusicGain);

                            if (bgm != null && isMixBgm != false) {
                                _AudioMix.VoiceMixEncode(_iSampleRateDef, 2, _RecorderBuffer, iMicLen);
                                result = _AudioMix.MusicMixEncode(44100, 2, bgm, bgm.length);
                                showAmplitude(_RecorderBuffer, iMicLen, micProgressBar, fMicGain);
                                showAmplitude(bgm, bgm.length, musicProgressBar, fMusicGain);
                                drawWavefrom();
                            } else if (bgm == null && mMusicPlayer.isPCMDataEos() && mBackLoop) {
                                Log.i("@@@@@@@@@@@@", "music finish.....");
                                mMusicPlayer.release();
                                mMusicPlayer.startPlayBackMusic();
                            } else {
                                result = _AudioMix.VoiceEncode(_iSampleRateDef, 2, _RecorderBuffer, iMicLen);
                                showAmplitude(_RecorderBuffer, iMicLen, micProgressBar, fMicGain);
                                drawWavefrom();
                            }


                        } else {
                            result = null;
                        }

                        if (result != null) {
                            try {
                                aacDataOutStream.write(result);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        });
        MixEncodeThread.start();

    }

    private void showAmplitude(byte[] sound, int len, ProgressBar progressBar, float gain) {
        //
        double sum = 0;
        int size = sound.length;

        for (int i = 0; i < size; i += 2) {
            // convert byte pair to int
            short buf1 = sound[i + 1];
            short buf2 = sound[i];

            buf1 = (short) ((buf1 & 0xff) << 8);
            buf2 = (short) (buf2 & 0xff);

            short res = (short) (buf1 | buf2);
            sum += res * res * gain;
        }

        final double amplitude = sum / len;
        progressBar.setProgress((int) Math.sqrt(amplitude));
    }

    private void drawWavefrom() {
        short[] shorts = new short[_RecorderBuffer.length / 2];
        ByteBuffer.wrap(_RecorderBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        mwaveCanvas.updateAudioData(shorts, _RecorderBuffer.length / 2, false);

    }

    class MusicPlayTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected Void doInBackground(String... params) {
            if (bgmIndex == 1)
                mMusicPlayer = new MusicPlayer(params[1]);
            else
                mMusicPlayer = new MusicPlayer(params[0]);
            mMusicPlayer.startPlayBackMusic();
            return null;
        }


        protected void onProgressUpdate(Integer... progress) {
        }


        protected void onPostExecute(Void result) {

        }

    }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d("####", "Headset is unplugged");
                        isMixBgm = false;
                        break;
                    case 1:
                        Log.d("####", "Headset is plugged");
                        isMixBgm = true;
                        break;
                    default:
                        Log.d("####", "I have no idea what the headset state is");
                }
            }
        }
    }

}

