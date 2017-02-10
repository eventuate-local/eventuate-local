
export const makeEvent = eventStr => {

  try {

    const { id: eventId, eventType, entityId, entityType, eventData: eventDataStr, swimlane, eventToken } = JSON.parse(eventStr);

    const eventData = JSON.parse(eventDataStr);

    const event = {
      eventId,
      eventType,
      entityId,
      swimlane,
      eventData,
      eventToken,
      entityType,
    };

    return { event };
  } catch (error) {
    return { error };
  }
};
