package org.elastos.ela;

import java.math.BigInteger;


import org.elastos.ela.bitcoinj.Sha256Hash;
import org.elastos.ela.bitcoinj.Utils;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.RandomDSAKCalculator;

/**
 * Created by nan on 18/1/14.
 */
public class SignTool {
    public static byte[] doSign(byte[] data, byte[] privateKey) {
        BigInteger privateKeyForSigning = new BigInteger(1,privateKey) ;
        //ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECDSASigner signer = new ECDSASigner(new RandomDSAKCalculator());
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, ECKey.CURVE);
        signer.init(true, privKey);

        while(true){
            BigInteger[] components = signer.generateSignature(Sha256Hash.hash(data));
            byte[] r = Utils.bigIntegerToBytes(components[0],32);
            byte[] s = Utils.bigIntegerToBytes(components[1],32);
            //byte[] r = components[0].abs().toByteArray();
            //byte[] s = components[1].abs().toByteArray();
            if(r.length>32||s.length>32) continue;

            byte[] signature = new byte[r.length+s.length];
            System.arraycopy(r,0,signature,0,r.length);
            System.arraycopy(s,0,signature,r.length,s.length);
            return signature;
        }

    }

    public static boolean verify(byte[] msg,byte[] sig,byte[] pub){
    	if(sig.length != 64 ) {
    		return false;
    	}
    	byte rb[] = new byte[sig.length/2];
    	byte sb[] = new byte[sig.length/2];
    	System.arraycopy(sig, 0, rb, 0, rb.length);
    	System.arraycopy(sig, sb.length, sb, 0, sb.length);
    	BigInteger r = parseBigIntegerPositive(new BigInteger(rb),rb.length * 8);
    	BigInteger s =  parseBigIntegerPositive(new BigInteger(sb),rb.length * 8);
    	
    	msg = Sha256Hash.hash(msg);
    	X9ECParameters curve = SECNamedCurves.getByName("secp256r1");
    	ECDSASigner singer = new ECDSASigner();
    	ECPublicKeyParameters publicKey = new ECPublicKeyParameters(curve.getCurve().decodePoint(pub),ECKey.CURVE);
    	singer.init(false, publicKey);
    	return singer.verifySignature(msg, r, s);
    }
    
    public static BigInteger parseBigIntegerPositive( BigInteger b,int bitlen) {
        if (b.compareTo(BigInteger.ZERO) < 0)
            b = b.add(BigInteger.ONE.shiftLeft(bitlen));
        return b;
    }
}

