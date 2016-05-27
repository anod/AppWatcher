package com.anod.appwatcher.market;

import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.HurlStack;

/**
 * @author alex
 * @date 2015-02-23
 */
public class Network extends BasicNetwork {
    public Network() {
        super(new HurlStack(), new ByteArrayPool(1024 * 256));
    }
}
