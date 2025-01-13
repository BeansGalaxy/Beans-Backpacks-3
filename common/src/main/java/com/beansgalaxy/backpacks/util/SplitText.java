package com.beansgalaxy.backpacks.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class SplitText {
      private final FormattedCharSequence sequence;
      private final List<FormattedCharSequence> lines = new ArrayList<>();
      private ImmutableList.Builder<Sink> current = ImmutableList.builder();

      public SplitText(FormattedCharSequence sequence) {
            this.sequence = sequence;
      }

      public List<FormattedCharSequence> split() {
            sequence.accept((pPositionInCurrentSequence, pStyle, pCodePoint) -> {
                  if (Character.isWhitespace(pCodePoint))
                        dump();
                  else current.add(new Sink(pStyle, pCodePoint));
                  return true;
            });

            buildCurrent(lines::add);
            return lines;
      }

      private void dump() {
            buildCurrent(lines::add);
            current = ImmutableList.builder();
      }

      private void buildCurrent(Consumer<FormattedCharSequence> addText) {
            ImmutableList<Sink> build = current.build();
            if (build.isEmpty() || onlyWhiteSpace(build))
                  return;

            FormattedCharSequence formatted = charSink -> {
                  for (int i = 0; i < build.size(); i++) {
                        Sink sink = build.get(i);
                        if (!charSink.accept(i, sink.style, sink.code))
                              return false;
                  }

                  return true;
            };

            addText.accept(formatted);
      }

      private boolean onlyWhiteSpace(ImmutableList<Sink> build) {
            for (Sink sink : build)
                  if (!Character.isWhitespace(sink.code))
                        return false;

            return true;
      }

      private record Sink(Style style, int code) {

      }

      FormattedCharSequence SPACE = FormattedCharSequence.forward(" ", Style.EMPTY);

      public int noCropWidth(Font font, int width, List<FormattedCharSequence> list) {
            if (lines.isEmpty())
                  return width;

            int size = lines.size();
            if (size > 2)
                  return smartArrange(font, width, list);

            FormattedCharSequence sequence = size == 1
                                 ? lines.getFirst()
                                 : FormattedCharSequence.composite(lines.get(0), SPACE, lines.get(1));

            list.add(sequence);
            int fWidth = font.width(sequence);
            return Math.max(fWidth, width);
      }

      private int simpleArrange(Font font, int width, List<FormattedCharSequence> list) {
            FormattedCharSequence currentLine = lines.getFirst();

            for (int i = 1; i < lines.size(); i++) {
                  FormattedCharSequence line = lines.get(i);
                  FormattedCharSequence pair = FormattedCharSequence.composite(currentLine, SPACE, line);
                  int pairWidth = font.width(pair);
                  if (pairWidth > width * 1.2) {
                        int currentWidth = font.width(currentLine);
                        if (currentWidth > width)
                              width = currentWidth;
                        list.add(currentLine);
                        currentLine = line;
                  }
                  else currentLine = pair;
            }

            int currentWidth = font.width(currentLine);
            if (currentWidth > width)
                  width = currentWidth;

            list.add(currentLine);
            return width;
      }

      private int smartArrange(Font font, int width, List<FormattedCharSequence> list) {
            int max = lines.stream().mapToInt(font::width).max().orElse(0);
            int newWidth = Math.max(width, max);
            int sWidth = font.width(SPACE) / 2;

            Iterator<FormattedCharSequence> iterator = Lists.reverse(lines).iterator();
            ArrayList<FormattedCharSequence> aggressive = Lists.newArrayList(iterator.next());
            FormattedCharSequence current = iterator.next();
            FormattedCharSequence next = iterator.next();

            while (!FormattedCharSequence.EMPTY.equals(current)) {
                  int nWidth = font.width(next);
                  int cWidth = font.width(current);
                  int lWidth = font.width(aggressive.getFirst());

                  int nextWidth = nWidth + sWidth + cWidth;
                  int lastWidth = lWidth + sWidth + cWidth;
                  int target = (nextWidth + lastWidth + cWidth + newWidth) / 4;
                  int n = Math.abs(nextWidth - target);
                  int l = Math.abs(lastWidth - target) - 7;
                  int c = Math.abs(cWidth - target);
                  int min = Math.min(l, Math.min(c, n));

                  if (min == l) {

                        FormattedCharSequence pair = FormattedCharSequence.composite(current, SPACE, aggressive.getFirst());
                        aggressive.set(0, pair);
                        current = next;
                        next = iterator.hasNext() ? iterator.next() : FormattedCharSequence.EMPTY;
                        continue;
                  }

                  if (min == n) {
                        if (FormattedCharSequence.EMPTY.equals(next)) {
                              aggressive.add(current);
                              break;
                        }



                        FormattedCharSequence pair = FormattedCharSequence.composite(next, SPACE, current);
                        aggressive.add(pair);
                        if (iterator.hasNext()) {
                              current = iterator.next();
                              next = iterator.hasNext() ? iterator.next() : FormattedCharSequence.EMPTY;
                              continue;
                        }

                        break;
                  }

                  aggressive.set(0, current);
                  current = next;
                  next = iterator.hasNext() ? iterator.next() : FormattedCharSequence.EMPTY;
            }

            int maxWidth = newWidth;
            for (int i = aggressive.size() - 1; i >= 0; i--) {
                  FormattedCharSequence sequence = aggressive.get(i);
                  int seqWidth = font.width(sequence);
                  if (seqWidth > maxWidth)
                        maxWidth = seqWidth;

                  list.add(sequence);
            }

            return maxWidth;
      }

      private int aggressiveArrange(Font font, int width, List<FormattedCharSequence> list) {
            int max = lines.stream().mapToInt(font::width).max().orElse(0);
            int newWidth = Math.max(width, max);

            ArrayList<FormattedCharSequence> aggressive = new ArrayList<>();
            FormattedCharSequence currentLine = lines.getLast();
            for (int i = lines.size() - 2; i >= 0; i--) {
                  FormattedCharSequence line = lines.get(i);
                  FormattedCharSequence pair = FormattedCharSequence.composite(line, SPACE, currentLine);
                  int pairWidth = font.width(pair);
                  if (pairWidth < newWidth) {
                        currentLine = pair;
                  }
                  else {
                        newWidth = pairWidth;
                        aggressive.add(pair);
                        i--;

                        if (i < 0) {
                              currentLine = null;
                              break;
                        }

                        currentLine = lines.get(i);
                  }
            }

            if (currentLine != null) {
                  int currentWidth = font.width(currentLine);
                  if (currentWidth > newWidth)
                        newWidth = currentWidth;

                  aggressive.add(currentLine);
            }

            for (int i = aggressive.size() - 1; i >= 0; i--)
                  list.add(aggressive.get(i));

            return newWidth;
      }

}
