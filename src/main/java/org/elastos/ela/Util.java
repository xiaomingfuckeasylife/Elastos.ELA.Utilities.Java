package org.elastos.ela;

import org.elastos.ela.bitcoinj.Base58;
import org.elastos.ela.bitcoinj.Sha256Hash;
import org.elastos.ela.bitcoinj.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by nan on 18/1/11.
 * TODO:
 */
public class Util {
    public static void WriteVarUint(DataOutputStream writer, long value) throws IOException {
        byte[] buf =new byte[9];
        if (value < 0xFD) {
            writer.writeByte((byte)value);
        } else if (value <= 0xFFFF) {
            writer.writeByte(0xFD);
            short s = FormatTransfer.reverseShort((short)value);
            writer.writeShort(s);
        } else if (value <= 0xFFFFFFFFL) {
            writer.writeByte(0xFE);
            int n = Integer.reverseBytes((int)value);
            writer.writeInt(n);
        } else {
            writer.writeByte(0xFE);

            long l = Long.reverseBytes(value);
            writer.writeLong(l);
        }
        return ;
    }
    public static void WriteVarBytes(DataOutputStream writer,byte[] value) throws IOException {
        WriteVarUint(writer,value.length);
        writer.write(value);
    }

    /**
     * 地址到 公钥/脚本 哈希 转换 可逆(ToAddress)
     * @param address
     * @return program hash 21byte
     */
    public static byte[]  ToScriptHash(String address ){

        byte[] decoded = Base58.decodeChecked(address);

        BigInteger bi = new BigInteger(decoded);
        byte[] ph = new byte[21];
        System.arraycopy(bi.toByteArray(),0,ph,0,21);

        return ph;
    }

    /**
     * 检查地址合法性
     * @param address
     * @return
     */
    public static boolean checkAddress(String address){
        try{
            byte[] sh = ToScriptHash(address);
            if(sh[0]!=33&&sh[0]!=18) return false;
            else return true;
        }catch (Exception ex){
            return false;
        }

    }


    /**
     * 公钥/脚本合约 到 公钥/脚本合约 哈希 转换 单向
     * @param code
     * @param signType
     * @return
     */
    public static byte[] ToCodeHash(byte[] code, int signType) {

        byte[] f = Utils.sha256hash160(code);
        byte[] g = new byte[f.length+1];

        if (signType == 1) {
            g[0] = 33;
            System.arraycopy(f,0,g,1,f.length);
        } else if (signType == 2) {
            g[0] = 18;
        } else{
            return null;
        }
        System.arraycopy(f,0,g,1,f.length);
        return g;

    }

    /**
     * 公钥/脚本 哈希 到地址转换 可逆（ToScriptHash)
     * @param programHash
     * @return
     */
    public static String ToAddress(byte[] programHash){
        byte[] f = Sha256Hash.hashTwice(programHash);
        byte[] g = new byte[programHash.length+4];
        System.arraycopy(programHash,0,g,0,programHash.length);
        System.arraycopy(f,0,g,programHash.length,4);

        //BigInteger bi = new BigInteger(g);

        return Base58.encode(g);
    }

    public static byte[] CreateSingleSignatureRedeemScript(byte[] pubkey) {
        byte[] script = new byte[35];
        script[0] = 33;
        System.arraycopy(pubkey,0,script,1,33);
        script[34] = (byte)0xAC;

        return script;
    }
    public static void sortByteArrayArrayUseRevertBytesSequence(byte[][] hashes) {
        Arrays.sort(hashes,new Comparator(){

            public int compare(Object o1, Object o2) {
                int ret;
                byte[] ba1 = (byte[])o1;
                byte[] ba2 = (byte[])o2;
                for(int i=ba1.length-1;i>=0;i--){
                    ret = (ba1[i]&0xff) - (ba2[i]&0xff);
                    if(ret !=0 ) return ret;

                }
                return 0;
            }
        });
    }

}

