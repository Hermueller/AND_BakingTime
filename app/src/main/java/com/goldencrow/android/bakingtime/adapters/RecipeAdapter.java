package com.goldencrow.android.bakingtime.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.goldencrow.android.bakingtime.R;
import com.goldencrow.android.bakingtime.entities.Ingredient;
import com.goldencrow.android.bakingtime.entities.Recipe;
import com.goldencrow.android.bakingtime.utils.EntityUtil;
import com.squareup.picasso.Picasso;

/**
 * Adapter for the MainActivity.
 * It handles the recipe-cards for the RecyclerView.
 *
 * @author Philipp Hermüller
 * @version 2018.3.14
 * @since 1.0
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /**
     * This key is used to store the Recipe_Id from the bookmark in SharedPreferences.
     * <p>
     * This is used to style the bookmarks appropriate after the app restarts.
     */
    private static final String BOOKMARK_ID_KEY = "bookmark_id";

    /**
     * This key is used to store the name of the recipe in SharedPreferences.
     * <p>
     * This key will be used by the widget to set the favorite recipe.
     *
     * @see com.goldencrow.android.bakingtime.FavoriteRecipeWidget#updateAppWidget(Context, AppWidgetManager, int, String, String)
     */
    public static final String BOOKMARK_TITLE_KEY = "bookmark_title";

    /**
     * <p>This key is used to store the list of ingredients (in form of a string) in the
     * SharedPreferences.</p>
     * <p>
     * The list of ingredients is generated by
     * {@link EntityUtil#getAllIngredientsAsAnEnumerationString(Ingredient[])}.
     *
     * @see com.goldencrow.android.bakingtime.FavoriteRecipeWidget#updateAppWidget(Context, AppWidgetManager, int, String, String)
     */
    public static final String BOOKMARK_INGREDIENTS_KEY = "bookmark_ingredients";

    /**
     * Context of the activity which initializes the RecyclerList.
     */
    private final Context mContext;

    /**
     * Contains all steps which will be displayed in the list.
     * It will be initialized by an intern method.
     *
     * @see #setRecipes(Recipe[])
     */
    private Recipe[] mRecipes;

    /**
     * contains the Favorite Recipe.
     */
    private final SharedPreferences mSharedPreferences;

    /**
     * stores the ID from the current favorite recipe.
     */
    private final int mBookmarkId;

    /**
     * stores the entire Bookmark ImageView.
     * <p>
     * This will be used to set the old Bookmark to the default size if another recipe was
     * selected as a favorite recipe.
     */
    private ImageView mBookmarkedView;

    /**
     * Handles the click on a recipe card.
     *
     * @see RecipeViewHolder#applyClickListenerToOpenRecipeDetail()
     */
    private final RecipeOnClickListener mRecipeClick;

    /**
     * Interface for the click on the recipe-card.
     */
    public interface RecipeOnClickListener {
        void OnClick(Recipe recipe);
    }

    /**
     * The constructor initializes the context and click-listener, as well as retrieving
     * the ID from the favorite recipe.
     *
     * @param context       the Context from where the RecyclerView is created.
     * @param recipeClick   the Click-Listener which handles a click on the card-item.
     */
    public RecipeAdapter(Context context, RecipeOnClickListener recipeClick) {
        this.mContext = context;
        this.mRecipeClick = recipeClick;

        mSharedPreferences = getSharedPreferences();

        final int favoriteRecipeNotFoundIndex = -1;
        mBookmarkId = mSharedPreferences.getInt(BOOKMARK_ID_KEY, favoriteRecipeNotFoundIndex);
    }

    /**
     * This method inflates the appropriate layout for the list-item.
     * <p>
     * But because every recipe-card (item) in the list has to look the same, there
     * won't be a differentiation from one to another.
     *
     * @param parent    the parent-viewGroup which owns this view.
     * @param viewType  specifies which layout the view on this position should have.
     * @return          a new RecipeViewHolder object.
     */
    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_card_layout, parent, false);

        RecipeViewHolder holder = new RecipeViewHolder(view);

        // store the image as tag for the espresso test.
        holder.mBackgroundImage.setTag(R.drawable.bakings);

        return holder;
    }

    /**
     * This method sets the information from the recipe at the current position into the
     * appropriate view and therefore fills them with text and info.
     * <p>
     * It also initializes the favorite recipe.
     *
     * @param holder    the ViewHolder where all the views are located.
     * @param position  the position of the item in the list.
     */
    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = mRecipes[position];
        int spaceForRecipeIntro = 1;

        // If the recipe has a thumbnail image then display it as the
        //    background image of the card.
        if (recipe.getImage() != null && !recipe.getImage().isEmpty()) {
            Picasso.with(mContext)
                    .load(recipe.getImage())
                    .into(holder.mBackgroundImage);
            // store the image as tag for the espresso test.
            holder.mBackgroundImage.setTag(recipe.getImage());
        } else {
            // , otherwise display an error-message showing that no
            //    image was found.
            holder.mErrorTv.setVisibility(View.VISIBLE);
        }

        // Set all the information for the front-side of the card into the corresponding views.
        holder.mTitleTv.setText(recipe.getName());
        holder.mBookmarkIv.setTag(recipe.getId());
        holder.mServingsTv.setText(String.valueOf(recipe.getServings()));
        holder.mStepsTv.setText(String.valueOf(recipe.getSteps().length - spaceForRecipeIntro));

        // Set all the information for the back-side of the card into the corresponding views.
        holder.mBackCardTitleTv.setText(recipe.getName());
        holder.mBackCardIngredientsTv.setText(
                EntityUtil.getAllIngredientsAsAnEnumerationString(recipe.getIngredients()));

        // If the current recipe is the favorite recipe, then make the bookmark bigger and
        //    remember this item, so that it's bookmark can be changed later on.
        if (recipe.getId() == mBookmarkId) {
            toggleImageHeight(holder.mBookmarkIv);
            mBookmarkedView = holder.mBookmarkIv;
        }
    }

    /**
     * Tells how many items the adapter currently holds.
     *
     * @return  the number of items in the list.
     */
    @Override
    public int getItemCount() {
        return mRecipes != null ? mRecipes.length : 0;
    }

    /**
     * Initializes the recipes for the list.
     *
     * @param recipes   the array of recipes which will be displayed in the list.
     */
    public void setRecipes(Recipe[] recipes) {
        mRecipes = recipes;
        notifyDataSetChanged();
    }

    /**
     * Changes the height of the bookmark from the recipe-card.
     * A long bookmark means that the current recipe is the favorite one,
     * while a short bookmark means nothing.
     * <p>
     * Note: The image is needed even if the recipe is not a favorite one.
     * The reason for this is that the onClick-method is on the bookmark image.
     *
     * @param view  the bookmark ImageView.
     */
    private void toggleImageHeight(ImageView view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        float markedHeight =
                mContext.getResources().getDimension(R.dimen.bookmark_marked_height);
        float unmarkedHeight =
                mContext.getResources().getDimension(R.dimen.bookmark_unmarked_height);

        // If the current height isn't the height of a favorite recipe,
        //    then change the height to the one of a favorite recipe.
        if (view.getHeight() != markedHeight) {
            params.height = (int) markedHeight;
        } else {
            params.height = (int) unmarkedHeight;
        }

        // reapply the changed LayoutParams.
        view.setLayoutParams(params);
    }

    /**
     * Retrieves a SharedPreferences object from the context which was passed to the constructor.
     *
     * @return  the SharedPreferences of the given context.
     */
    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * The class representing an item in the list.
     *
     * @author Philipp Hermüller
     * @version 2018.3.14
     * @since 1.0
     */
    class RecipeViewHolder extends RecyclerView.ViewHolder {

        // Sides of the card.
        final ConstraintLayout front_card_layout;
        final ConstraintLayout back_card_layout;

        // Views on the front side of the card.
        final ImageView mBackgroundImage;
        final ImageView mBookmarkIv;
        final TextView mErrorTv;
        final TextView mTitleTv;
        final TextView mServingsTv;
        final TextView mStepsTv;
        final Button mIngredientsBtn;

        // Views on the back side of the card.
        final TextView mBackCardTitleTv;
        final TextView mBackCardIngredientsTv;

        /**
         * Initializes the View's from the card-layout and applies the onClickListener's.
         *
         * @param cardView  the card (View) representing a single item in the list.
         */
        RecipeViewHolder(final View cardView) {
            super(cardView);

            front_card_layout = cardView.findViewById(R.id.front_card_layout);
            back_card_layout = cardView.findViewById(R.id.back_card_layout);

            mBackgroundImage = cardView.findViewById(R.id.card_background_image);
            mBookmarkIv = cardView.findViewById(R.id.bookmark_iv);
            mErrorTv = cardView.findViewById(R.id.error_tv);
            mTitleTv = cardView.findViewById(R.id.card_title_tv);
            mServingsTv = cardView.findViewById(R.id.servings_tv);
            mStepsTv = cardView.findViewById(R.id.steps_tv);
            mIngredientsBtn = cardView.findViewById(R.id.ingredients_btn);

            mBackCardTitleTv = cardView.findViewById(R.id.back_card_title_tv);
            mBackCardIngredientsTv = cardView.findViewById(R.id.back_card_ingredients_tv);

            // A click on the bookmark ImageView changes a normal recipe into a favorite one.
            mBookmarkIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    float markedHeight = mContext.getResources()
                            .getDimension(R.dimen.bookmark_marked_height);
                    if (mBookmarkIv.getHeight() != markedHeight) {
                        // sends a Broadcast to the Widget which will update all widgets
                        //    to display the info's from the "new" favorite recipe instead
                        //    of the old one.
                        EntityUtil.setFavoriteRecipeToWidget(mContext,
                                mTitleTv.getText().toString(),
                                mBackCardIngredientsTv.getText().toString());

                        //========
                        // save the info's from this (favorite) recipe for future uses.
                        //========
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.clear();
                        editor.putInt(BOOKMARK_ID_KEY, (int) mBookmarkIv.getTag());
                        editor.putString(BOOKMARK_TITLE_KEY, mTitleTv.getText().toString());
                        editor.putString(BOOKMARK_INGREDIENTS_KEY,
                                mBackCardIngredientsTv.getText().toString());
                        editor.apply();
                        //========
                    }
                    toggleImageHeight(mBookmarkIv);
                    if (mBookmarkedView != null) {
                        toggleImageHeight(mBookmarkedView);
                    }
                    mBookmarkedView = mBookmarkIv;
                }
            });

            // opens the recipe-step-Layout if the card (front-side) was clicked on.
            applyClickListenerToOpenRecipeDetail();

            //========
            // Handle the card-flip animations:
            //========
            View.OnClickListener cardRotateClick = createCardFlipOnClickListener(cardView);
            mIngredientsBtn.setOnClickListener(cardRotateClick);
            back_card_layout.setOnClickListener(cardRotateClick);
            //========
        }

        /**
         * If the front-side of the recipe card was clicked on, then the OnClickListener
         * (which is applied here) will call the onClickMethod which was passed in from the
         * parent activity.
         */
        private void applyClickListenerToOpenRecipeDetail() {
            front_card_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    mRecipeClick.OnClick(mRecipes[position]);
                }
            });
        }

        /**
         * Code from: https://stackoverflow.com/a/46111960
         * <p>
         * Handles the rotating animation of the card and returns an onClickListener which
         * initiates this animation.
         * <p>
         * This will be needed to flip the card over. On the backside of the card are the ingredients
         * to the recipe.
         *
         * @param cardView  the view (cardView) which is going to rotate/flip.
         * @return          the generated onClickListener which initiates the rotation/flip.
         */
        private View.OnClickListener createCardFlipOnClickListener(final View cardView) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int timeForHalfFlipInMillis = 500;
                    String propertyScaleX = "scaleX";
                    float fullView = 1f;
                    float flatView = 0f;

                    final ObjectAnimator fullToFlatAnimation = ObjectAnimator.ofFloat(
                            cardView, propertyScaleX, fullView, flatView);
                    final ObjectAnimator flatToFullAnimation = ObjectAnimator.ofFloat(
                            cardView, propertyScaleX, flatView, fullView);

                    fullToFlatAnimation.setInterpolator(new DecelerateInterpolator());
                    flatToFullAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    fullToFlatAnimation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            if (front_card_layout.getVisibility() != View.INVISIBLE) {
                                front_card_layout.setVisibility(View.INVISIBLE);
                                back_card_layout.setVisibility(View.VISIBLE);
                            } else {
                                front_card_layout.setVisibility(View.VISIBLE);
                                back_card_layout.setVisibility(View.INVISIBLE);
                            }

                            flatToFullAnimation.start();
                        }
                    });
                    fullToFlatAnimation.setDuration(timeForHalfFlipInMillis);
                    flatToFullAnimation.setDuration(timeForHalfFlipInMillis);
                    fullToFlatAnimation.start();
                }
            };
        }
    }

}
