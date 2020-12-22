// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {
    // private static final int SMALLEST_DURATION = 15;

    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        /** the request is too long */
        if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
            return Arrays.asList();
        }

        /** there are no events or no attendes */
        if (events.isEmpty() || request.getAttendees().isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        } 

        /** Algorithm
         * 1. Create an array and set of possible meeting times and all events in 30min blocks for the whole day
         * 2. Remove all the event times from the possible meeting times collection
         * 3. Piece meeting times together to make longest possible blocks
         * 4. Remove any blocks which are less than the requested meeting duration
         * 5. If there are no possible meeting times, repeat without optional attendees (if there are)
        */

        ArrayList<TimeRange> allTimes = new ArrayList<TimeRange>();
        for (int i = 0; i < TimeRange.END_OF_DAY; i += 30) {
            allTimes.add(TimeRange.fromStartDuration(i, 30));
        }

        Set<TimeRange> eventTimes = new HashSet<TimeRange>();
        for (Event ev: events) {
            boolean attendBoth = false;
            loop: for (String eventAttendee: ev.getAttendees()) {
                for (String meetingAttendee: request.getAttendees()) {
                    if (eventAttendee == meetingAttendee) {
                        attendBoth = true;
                        break loop;
                    }
                }
            }

            if (!attendBoth) {
                break;
            }

            TimeRange when = ev.getWhen();
            int j = when.duration();
            while (j != 0) {
                eventTimes.add(TimeRange.fromStartDuration(when.end() - j, 30));
                j -= 30;
            }
        }

        allTimes.removeAll(eventTimes);

        ArrayList<TimeRange> meetingTimes = new ArrayList<TimeRange>();
        for (int i = 0; i < allTimes.size(); i++) {
            int start = allTimes.get(i).start();
            int end = allTimes.get(i).end();
            for (int newStart = end; i < allTimes.size() && newStart == allTimes.get(i).end(); newStart += 30) {
                end += 30;
                i++;
            }

            if (end - start - 30 >= request.getDuration()) {
                meetingTimes.add(TimeRange.fromStartEnd(start, end - 30, false));
            }

            i--;
        }

        return meetingTimes;
    }
}
