package info.anodsplace.playstore

import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.ByteArrayPool
import com.android.volley.toolbox.HurlStack

/**
 * @author alex
 * *
 * @date 2015-02-23
 */
class Network : BasicNetwork(HurlStack(), ByteArrayPool(1024 * 256))
