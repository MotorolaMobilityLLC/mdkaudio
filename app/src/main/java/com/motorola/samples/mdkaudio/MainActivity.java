/**
 * Copyright (c) 2016 Motorola Mobility, LLC.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.motorola.samples.mdkaudio;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.motorola.mod.ModDevice;

/**
 * A class to represent main activity.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    public static final String MOD_UID = "mod_uid";

    /**
     * Instance of MDK Personality Card interface
     */
    private Personality personality;

    /** Handler for events from mod device */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Personality.MSG_MOD_DEVICE:
                    /** Mod attach/detach */
                    ModDevice device = personality.getModDevice();
                    onModDevice(device);
                    break;
                default:
                    Log.i(Constants.TAG, "MainActivity - Un-handle mod events: " + msg.what);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        LinearLayout dipTitle = (LinearLayout)findViewById(R.id.layout_dip_description_title);
        dipTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout dipDescription = (LinearLayout)findViewById(R.id.layout_dip_description);
                ImageView imgExpand = (ImageView)findViewById(R.id.imageview_description_img);

                if (dipDescription.getVisibility() == View.GONE) {
                    dipDescription.setVisibility(View.VISIBLE);
                    imgExpand.setImageResource(R.drawable.ic_expand_less);
                } else {
                    dipDescription.setVisibility(View.GONE);
                    imgExpand.setImageResource(R.drawable.ic_expand_more);
                }

                dipDescription.setPivotY(0);
                ObjectAnimator.ofFloat(dipDescription, "scaleY", 0f, 1f).setDuration(300).start();
            }
        });

        TextView textView = (TextView)findViewById(R.id.mod_external_dev_portal);
        if (textView != null) {
            textView.setOnClickListener(this);
        }

        textView = (TextView)findViewById(R.id.mod_source_code);
        if (textView != null) {
            textView.setOnClickListener(this);
        }

        Button button = (Button)findViewById(R.id.status_phone_ringtone);
        if (button != null) {
            button.setOnClickListener(this);
        }

        button = (Button)findViewById(R.id.status_notification_ringtone);
        if (button != null) {
            button.setOnClickListener(this);
        }

        button = (Button)findViewById(R.id.status_play_music);
        if (button != null) {
            button.setOnClickListener(this);
        }

        button = (Button)findViewById(R.id.status_dialer);
        if (button != null) {
            button.setOnClickListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            /** Get the UUID from attached mod device */
            String uid = getString(R.string.na);
            if (personality != null
                    && personality.getModDevice() != null
                    && personality.getModDevice().getUniqueId() != null) {
                uid = personality.getModDevice().getUniqueId().toString();
            }

            startActivity(new Intent(this, AboutActivity.class).putExtra(MOD_UID, uid));
            return true;
        }

        if (id == R.id.action_policy) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_PRIVACY_POLICY)));
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        releasePersonality();

        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        initPersonality();
    }

    private void initPersonality() {
        if (null == personality) {
            personality = new Personality(this);

            /** Register handler to get event and data update */
            personality.registerListener(handler);
        }
    }

    private void releasePersonality() {
        if (null != personality) {
            personality.onDestroy();
            personality = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /** Mod device attach/detach */
    public void onModDevice(ModDevice device) {
        /** Moto Mods Status */
        /**
         * Get mod device's Product String, which should correspond to
         * the product name or the vendor internal's name.
         */
        TextView tvName = (TextView) findViewById(R.id.mod_name);
        if (null != tvName) {
            if (null != device) {
                tvName.setText(device.getProductString());

                if ((device.getVendorId() == Constants.VID_MDK
                        && device.getProductId() == Constants.PID_MDK_AUDIO)
                        || device.getVendorId() == Constants.VID_DEVELOPER)  {
                    tvName.setTextColor(getColor(R.color.mod_match));
                } else {
                    tvName.setTextColor(getColor(R.color.mod_mismatch));
                }
            } else {
                tvName.setText(getString(R.string.na));
                tvName.setTextColor(getColor(R.color.mod_na));
            }
        }

        /**
         * Get mod device's Vendor ID. This is assigned by the Motorola
         * and unique for each vendor.
         */
        TextView tvVid = (TextView) findViewById(R.id.mod_status_vid);
        if (null != tvVid) {
            if (device == null
                    || device.getVendorId() == Constants.INVALID_ID) {
                tvVid.setText(getString(R.string.na));
            } else {
                tvVid.setText(String.format(getString(R.string.mod_pid_vid_format),device.getVendorId()));
            }
        }

        /** Get mod device's Product ID. This is assigned by the vendor */
        TextView tvPid = (TextView) findViewById(R.id.mod_status_pid);
        if (null != tvPid) {
            if (device == null
                    || device.getProductId() == Constants.INVALID_ID) {
                tvPid.setText(getString(R.string.na));
            } else {
                tvPid.setText(String.format(getString(R.string.mod_pid_vid_format),device.getProductId()));
            }
        }

        /** Get mod device's version of the firmware */
        TextView tvFirmware = (TextView) findViewById(R.id.mod_status_firmware);
        if (null != tvFirmware) {
            if (null != device && null != device.getFirmwareVersion()
                    && !device.getFirmwareVersion().isEmpty()) {
                tvFirmware.setText(device.getFirmwareVersion());
            } else {
                tvFirmware.setText(getString(R.string.na));
            }
        }

        /**
         * Get the default Android application associated with the currently attached mod,
         * as read from the mod hardware manifest.
         */
        TextView tvPackage = (TextView) findViewById(R.id.mod_status_package_name);
        if (null != tvPackage) {
            if (device == null
                    || personality.getModManager() == null) {
                tvPackage.setText(getString(R.string.na));
            } else {
                if (personality.getModManager() != null) {
                    String modPackage = personality.getModManager().getDefaultModPackage(device);
                    if (null == modPackage || modPackage.isEmpty()) {
                        modPackage = getString(R.string.name_default);
                    }
                    tvPackage.setText(modPackage);
                }
            }
        }

        /**
         * Set Audio Description text based on current state
         */
        TextView tvAudio = (TextView)findViewById(R.id.audio_text);
        if (tvAudio != null) {
            if (device == null) {
                tvAudio.setText(R.string.attach_pcard);
            } else if (device.getVendorId() == Constants.VID_DEVELOPER) {
                tvAudio.setText(R.string.status_audio_summary);
            } else if (device.getVendorId() == Constants.VID_MDK) {
                if (device.getProductId() == Constants.PID_MDK_AUDIO) {
                    tvAudio.setText(R.string.status_audio_summary);
                } else {
                    tvAudio.setText(R.string.mdk_switch);
                }
            } else {
                tvAudio.setText(getString(R.string.attach_pcard));
            }
            /**
             * Enable/Disable controls based on MDK-AUDIO or Developer Mode
             */
            boolean enabled = false;
            if (device != null) {
                if (device.getVendorId() == Constants.VID_DEVELOPER) {
                    enabled = true;
                } else if ((device.getVendorId() == Constants.VID_MDK) &&
                        (device.getProductId() == Constants.PID_MDK_AUDIO)) {
                    enabled = true;
                }
            }
            Button bRing = (Button) findViewById(R.id.status_phone_ringtone);
            if (bRing != null)
                bRing.setEnabled(enabled);
            Button bNoti = (Button) findViewById(R.id.status_notification_ringtone);
            if (bNoti != null)
                bNoti.setEnabled(enabled);
            Button bMusic = (Button) findViewById(R.id.status_play_music);
            if (bMusic != null)
                bMusic.setEnabled(enabled);
            Button bDial = (Button) findViewById(R.id.status_dialer);
            if (bDial != null)
                bDial.setEnabled(enabled);
        }
    }

    /** Check whether attached mod is a MDK based on VID/PID */
    private boolean isMDKMod(ModDevice device) {
        if (device == null) {
            // Mod not attached
            return false;
        } else if (device.getVendorId() == Constants.VID_DEVELOPER
                && device.getProductId() == Constants.PID_DEVELOPER) {
            // MDK in developer mode
            return true;
        } else {
            // Check MDK
            return device.getVendorId() == Constants.VID_MDK;
        }
    }

    /** Button click event from UI */
    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.mod_external_dev_portal:
                /** The Developer Portal link is clicked */
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_DEV_PORTAL)));
                break;
            case R.id.mod_source_code:
                /** The Buy Mods link is clicked */
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_SOURCE_CODE)));
                break;
            case R.id.status_phone_ringtone: {
                /** Phone ringtone button is clicked */
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                startActivity(intent);
            }
            break;
            case R.id.status_notification_ringtone: {
                /** Notification ringtone button is clicked */
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                startActivity(intent);
            }
            break;
            case R.id.status_play_music:
                /** Play music button is clicked */
                Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
                        Intent.CATEGORY_APP_MUSIC);
                startActivity(intent);
                break;
            case R.id.status_dialer:
                /** Dialer button is clicked */
                startActivity(new Intent(Intent.ACTION_DIAL));
                break;
            default:
                Log.i(Constants.TAG, "MainActivity - Un-handled button action.");
                break;
        }
    }
}
