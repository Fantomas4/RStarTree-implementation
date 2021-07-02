package utils;

import tree.Entry;
import tree.LeafEntry;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class ByteConvertible
{
        // https://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
        protected static byte[] longToBytes(long l)
        {
                byte[] result = new byte[Long.BYTES];
                for (int i = Long.BYTES - 1; i >= 0; --i)
                {
                        result[i] = (byte)(l & 0xFF);
                        l >>= Byte.SIZE;
                }
                return result;
        }

        protected static long bytesToLong(final byte[] b)
        {
                long result = 0;
                for (int i = 0; i < Long.BYTES; ++i)
                {
                        result <<= Byte.SIZE;
                        result |= (b[i] & 0xFF);
                }
                return result;
        }

        // https://stackoverflow.com/questions/2905556/how-can-i-convert-a-byte-array-into-a-double-and-back?answertab=votes#tab-top
        protected static byte[] doubleToBytes(double value)
        {
                byte[] bytes = new byte[Double.BYTES];
                ByteBuffer.wrap(bytes).putDouble(value);
                return bytes;
        }

        protected static double bytesToDouble(byte[] bytes)
        {
                return ByteBuffer.wrap(bytes).getDouble();
        }

        // https://stackoverflow.com/questions/1936857/convert-integer-into-byte-array-java
        protected static byte[] intToBytes(int value)
        {
                byte[] bytes = new byte[Integer.BYTES];
                ByteBuffer.wrap(bytes).putInt(value);
                return bytes;
        }

        protected static int bytesToInt(byte[] bytes)
        {
                return ByteBuffer.wrap(bytes).getInt();
        }

        protected static byte[] entriesToBytes(ArrayList<Entry> entries)
        {
                byte[] entriesAsBytes = new byte[Integer.BYTES + 1 + (IndexMetaData.maxEntriesInNode + 1) * Entry.BYTES];
                int destPos = 0;

                // Number of entries
                System.arraycopy(intToBytes(entries.size()), 0, entriesAsBytes, destPos, Integer.BYTES);
                destPos += Integer.BYTES;

                if (!entries.isEmpty())
                {
                        // are LeafEntries
                        entriesAsBytes[destPos] = (byte)(entries.get(0) instanceof LeafEntry ? 1 : 0);
                }
                else
                {
                        // are Entries
                        entriesAsBytes[destPos] = (byte) 0;
                }
                destPos += 1;

                for (Entry entry : entries)
                {
                        // Every entry in the node
                        System.arraycopy(entry.toBytes(), 0, entriesAsBytes, destPos, Entry.BYTES);
                        destPos += Entry.BYTES;
                }

                return entriesAsBytes;
        }

        protected static ArrayList<Entry> entriesFromBytes(byte[] bytes)
        {
                byte[] sizeAsBytes = new byte[Integer.BYTES];
                int srcPos = 0;

                System.arraycopy(bytes, srcPos, sizeAsBytes, 0, sizeAsBytes.length);
                srcPos += sizeAsBytes.length;

                int size = bytesToInt(sizeAsBytes);
                if (size == 0)
                {
                        return null;
                }
                ArrayList<Entry> entries = new ArrayList<>();

                byte areLeafEntries = bytes[srcPos];
                srcPos += 1;

                for (int i = 0; i < size; ++i)
                {
                        byte[] entryAsBytes = new byte[Entry.BYTES];
                        System.arraycopy(bytes, srcPos, entryAsBytes, 0, Entry.BYTES);
                        srcPos += Entry.BYTES;
                        entries.add(areLeafEntries != 0 ? LeafEntry.fromBytes(entryAsBytes) : Entry.fromBytes(entryAsBytes));
                }

                return entries;
        }

        public abstract byte[] toBytes();

}
