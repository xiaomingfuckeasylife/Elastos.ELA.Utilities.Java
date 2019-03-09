package org.elastos.ela;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by nan on 18/1/10.
 */
public class Uint256 {

    final static int UINT256SIZE = 32;
    //uint
    byte[] Uint256 = new byte[UINT256SIZE];

    public Uint256(byte[] b){
        System.arraycopy(b,0,Uint256,0,32);
    }

    public void Serialize(DataOutputStream o) throws IOException {
        o.write(this.Uint256);
    }

}
