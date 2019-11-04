package players.PlayerTeamG;

import core.GameState;
import players.Player;
import players.optimisers.ParameterizedPlayer;
import utils.ElapsedCpuTimer;
import utils.Types;

import java.util.ArrayList;
import java.util.Random;

public class PlayerTeamG extends ParameterizedPlayer {

    /**
     * Random generator.
     */
    private Random m_rnd;

    /**
     * All actions available.
     */
    public Types.ACTIONS[] actions;

    /**
     * Params for this MCTS
     */
    public PlayerTeamGParams params;

    public PlayerTeamG(long seed, int id) {
        this(seed, id, new PlayerTeamGParams());
    }

    private ArrayList<Double> runTimes;

    public PlayerTeamG(long seed, int id, PlayerTeamGParams params) {
        super(seed, id, params);
        reset(seed, id);

        ArrayList<Types.ACTIONS> actionsList = Types.ACTIONS.all();
        actions = new Types.ACTIONS[actionsList.size()];
        int i = 0;
        for (Types.ACTIONS act : actionsList) {
            actions[i++] = act;
        }
        //for recording the runtimes of each player turns and to calculate the average
        runTimes= new ArrayList<Double>();
    }

    @Override
    public void reset(long seed, int playerID) {
        this.seed = seed;
        this.playerID = playerID;
        m_rnd = new Random(seed);

        this.params = (PlayerTeamGParams) getParameters();
        if (this.params == null) {
            this.params = new PlayerTeamGParams();
            super.setParameters(this.params);
        }
    }

    @Override
    public Types.ACTIONS act(GameState gs) {
        double timeAtStart = 0.0;
        double timeAtEnd = 0.0;
        // TODO update gs
        if (gs.getGameMode().equals(Types.GAME_MODE.TEAM_RADIO)) {
            int[] msg = gs.getMessage();
        }

        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(params.num_time);
        timeAtStart = ect.elapsedMillis();
        //System.out.println("Time at start of act(): "+ect);
        // Number of actions available
        int num_actions = actions.length;

        // Root of the tree
        SingleTreeNode m_root = new SingleTreeNode(params, m_rnd, num_actions, actions);
        m_root.setRootGameState(gs);

        //Determine the action using MCTS...
        m_root.mctsSearch(ect);

        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();

        // TODO update message memory
        timeAtEnd = ect.elapsedMillis();
        //System.out.println("Time at end of act(): "+ect);
        double timeTaken=timeAtEnd - timeAtStart;
        runTimes.add(timeTaken);
        System.out.println("Total time taken by player Team G: " + timeTaken + "ms in tick: "+gs.getTick());
        Double totalTimeTaken = 0.0;
        for(Double runTime : runTimes){
            totalTimeTaken+=runTime;
        }
        Double averageTimeTaken=totalTimeTaken/runTimes.size();
        System.out.println("Current average run time: "+averageTimeTaken);
        //... and return it.
        return actions[action];
    }

    @Override
    public int[] getMessage() {
        // default message
        int[] message = new int[Types.MESSAGE_LENGTH];
        message[0] = 1;
        return message;
    }

    @Override
    public Player copy() {
        return new PlayerTeamG(seed, playerID, params);
    }
}