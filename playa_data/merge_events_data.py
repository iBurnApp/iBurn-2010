import Levenshtein
import json

# Threshold under which to discard partial string matches
MATCH_THRESHOLD = .6

location_file = open('camp_locations.json')
events_file = open('events_data.json')

location_json = json.loads(location_file.read())
events_json = json.loads(events_file.read())

# Some entries in event_data are null, remove them before writing final json
null_event_indexes = []

# match name fields between entries in two files
for index, event in enumerate(events_json):
    max_match = 0
    max_match_location = ''
    if event != None and 'hosted_by_camp' in event:
        for location in location_json:
                match = Levenshtein.ratio(location['name'].strip(), event['hosted_by_camp']['name'].strip())
                if match > max_match:
                    max_match = match
                    max_match_location = location
        #print "Best match for " + event['name'] + " : " + max_match_location['name'] + " (confidence: " + str(max_match) + ")"
        if max_match > MATCH_THRESHOLD:
            # Match found
            event['latitude'] = max_match_location['latitude']
            event['longitude'] = max_match_location['longitude']
            event['location'] = max_match_location['location']
            event['matched_name'] = max_match_location['name']
    else:
        null_event_indexes.append(index)

# To remove null entries from list, we must move in reverse
# to preserve list order as we remove
null_event_indexes.reverse()
for index in null_event_indexes:
    events_json.pop(index)

result_file = open('./results/event_data_and_locations.json', 'w')
result_file.write(json.dumps(events_json, sort_keys=True, indent=4))
