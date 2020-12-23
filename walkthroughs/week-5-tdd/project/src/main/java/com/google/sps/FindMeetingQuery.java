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

import jdk.nashorn.internal.ir.RuntimeNode.Request;

public final class FindMeetingQuery {
    private static final int SMALLEST_DURATION = 15;

    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        // the request is too long
        if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
            return new ArrayList<>();
        }

        // there are no events
        if (events.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        } 

        // Algorithm
        //  1. Create an array and set of possible meeting times and all events in 30min blocks for the whole day
        //  2. Remove all the event times from the possible meeting times collection
        //  3. Piece meeting times together to make longest possible blocks
        //  4. Remove any blocks which are less than the requested meeting duration
        //  5. If there are no possible meeting times, repeat without optional attendees (if there are)
        

        ArrayList<TimeRange> allTimes = new ArrayList<>();
        for (int i = 0; i < TimeRange.END_OF_DAY; i += SMALLEST_DURATION) {
            allTimes.add(TimeRange.fromStartDuration(i, SMALLEST_DURATION));
        }

        Collection<String> meetingAttendees = request.getAttendees();
        Collection<String> optionalAttendees = request.getOptionalAttendees();
        Set<TimeRange> allEventTimes = new HashSet<>();
        Set<TimeRange> mandatoryEventTimes = new HashSet<>();
        for (Event ev: events) {
            boolean attendBoth = false;
            boolean optional = false;
            loop: for (String eventAttendee: ev.getAttendees()) {
                for (String meetingAttendee: meetingAttendees) {
                    if (eventAttendee.equals(meetingAttendee)) {
                        attendBoth = true;
                        break loop;
                    }
                }
                for (String optionalAttendee: optionalAttendees) {
                    if (eventAttendee.equals(optionalAttendee)) {
                        attendBoth = true;
                        optional = true;
                        break loop;
                    }
                }
            }

            if (attendBoth) {
                TimeRange when = ev.getWhen();
                int j = when.duration();
                while (j > 0) {
                    TimeRange eventTime = TimeRange.fromStartDuration(when.end() - j, SMALLEST_DURATION);
                    if (!optional) {
                        mandatoryEventTimes.add(eventTime);
                    }
                    allEventTimes.add(eventTime);

                    j -= SMALLEST_DURATION;
                }
            }
        }

        ArrayList<TimeRange> allTimesCopy = new ArrayList<>(allTimes);

        allTimes.removeAll(allEventTimes);
        allTimesCopy.removeAll(mandatoryEventTimes);

        ArrayList<TimeRange> meetingTimes = new ArrayList<>();
        meetingTimes = getMeetingTimes(allTimes, request);

        // If there are no possible meetings considering optional attendees,
        // only consider the mandatory attendees.
        if (meetingTimes.size() == 0) {
            meetingTimes = getMeetingTimes(allTimesCopy, request);
        }

        return meetingTimes;
    }
    
    public ArrayList<TimeRange> getMeetingTimes(ArrayList<TimeRange> allTimes, MeetingRequest request) {
        ArrayList<TimeRange> meetingTimes = new ArrayList<>();
        for (int i = 0; i < allTimes.size(); i++) {
            int start = allTimes.get(i).start();
            int end = allTimes.get(i).end();
            for (int newStart = end; i < allTimes.size() && newStart == allTimes.get(i).end(); newStart += SMALLEST_DURATION) {
                end += SMALLEST_DURATION;
                i++;
            }

            if (end - start - SMALLEST_DURATION >= request.getDuration()) {
                meetingTimes.add(TimeRange.fromStartEnd(start, end - SMALLEST_DURATION, false));
            }

            i--;
        }

        return meetingTimes;
    }
}
